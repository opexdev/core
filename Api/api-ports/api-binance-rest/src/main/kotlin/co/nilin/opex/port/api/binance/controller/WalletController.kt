package co.nilin.opex.port.api.binance.controller

import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.inout.TransactionHistoryResponse
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.port.api.binance.data.AssignAddressResponse
import co.nilin.opex.port.api.binance.data.DepositResponse
import co.nilin.opex.port.api.binance.data.Interval
import co.nilin.opex.port.api.binance.data.WithdrawResponse
import co.nilin.opex.port.api.binance.util.jwtAuthentication
import co.nilin.opex.port.api.binance.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@RestController
class WalletController(
    private val walletProxy: WalletProxy,
    private val bcGatewayProxy: BlockchainGatewayProxy
) {

    @GetMapping("/v1/capital/deposit/address")
    suspend fun assignAddress(
        @RequestParam("coin")
        coin: String,
        @RequestParam("network", required = false)
        network: String?,
        @RequestParam(name = "recvWindow", required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam(name = "timestamp")
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AssignAddressResponse {
        val response = bcGatewayProxy.assignAddress(securityContext.jwtAuthentication().name, coin)
        val address = if (response.addresses.isNotEmpty()) response.addresses[0] else null
        return AssignAddressResponse(address?.address ?: "", coin, "", "")
    }

    @GetMapping("/v1/capital/deposit/hisrec")
    suspend fun getDepositTransactions(
        @RequestParam("coin", required = false)
        coin: String?,
        @RequestParam("network", required = false)
        status: Int?,
        @RequestParam("startTime", required = false)
        startTime: Long?,
        @RequestParam("endTime", required = false)
        endTime: Long?,
        @RequestParam("offset", required = false)
        offset: Int?,
        @RequestParam("limit", required = false)
        limit: Int?,
        @RequestParam(name = "recvWindow", required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam(name = "timestamp")
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<DepositResponse> {
        val validLimit = limit ?: 1000
        val deposits = walletProxy.getDepositTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            coin,
            startTime ?: Interval.ThreeMonth.getDate().time,
            endTime ?: Date().time,
            if (validLimit > 1000 || validLimit < 1) 1000 else validLimit,
            offset ?: 0
        )

        val details = bcGatewayProxy.getDepositDetails(deposits.map { it.ref ?: "" })
        return matchDepositsAndDetails(deposits, details)
    }

    @GetMapping("/v1/capital/withdraw/history")
    suspend fun getWithdrawTransactions(
        @RequestParam("coin", required = false)
        coin: String,
        @RequestParam("withdrawOrderId", required = false)
        withdrawOrderId: String?,
        @RequestParam("status", required = false)
        withdrawStatus: Int?,
        @RequestParam("offset", required = false)
        offset: Int?,
        @RequestParam("limit", required = false)
        limit: Int?,
        @RequestParam("startTime", required = false)
        startTime: Long?,
        @RequestParam("endTime", required = false)
        endTime: Long?,
        @RequestParam(name = "recvWindow", required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam(name = "timestamp")
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<WithdrawResponse> {
        val validLimit = limit ?: 1000
        val response = walletProxy.getWithdrawTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            coin,
            startTime ?: Interval.ThreeMonth.getDate().time,
            endTime ?: Date().time,
            if (validLimit > 1000 || validLimit < 1) 1000 else validLimit,
            offset ?: 0
        )
        return response.map {
            val status = when (it.status) {
                "CREATED" -> 0
                "DONE" -> 1
                "REJECTED" -> 2
                else -> -1
            }

            WithdrawResponse(
                it.destAddress ?: "0xusEraDdReS",
                it.amount,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.createDate), ZoneId.systemDefault())
                    .toString()
                    .replace("T", " "),
                it.destCurrency ?: "",
                it.withdrawId?.toString() ?: "",
                "",
                it.destNetwork ?: "",
                1,
                status,
                it.appliedFee.toString(),
                3,
                it.withdrawId.toString(),
                if (status == 1 && it.acceptDate != null) it.acceptDate!! else it.createDate
            )
        }
    }

    private fun matchDepositsAndDetails(
        deposits: List<TransactionHistoryResponse>,
        details: List<DepositDetails>
    ): List<DepositResponse> {
        return deposits.map { deposit ->
            val detail = details.find { deposit.ref == it.hash }
            DepositResponse(
                deposit.amount,
                deposit.currency,
                detail?.chain ?: "",
                1,
                detail?.address ?: "0xusEraDdReS",
                null,
                deposit.ref ?: deposit.id.toString(),
                deposit.date,
                1,
                "1/1",
                "1/1",
                deposit.date
            )
        }
    }

}