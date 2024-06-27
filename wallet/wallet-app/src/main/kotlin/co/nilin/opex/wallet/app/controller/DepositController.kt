package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.app.dto.WithdrawHistoryResponse
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.service.WithdrawService
import co.nilin.opex.wallet.core.spi.DepositPersister
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/deposit")
class DepositController(private val depositPersister: DepositPersister,
                        private val transferService: TransferService) {


    @PostMapping("/history/{uuid}")
    suspend fun getDepositTransactionsForUser(
            @PathVariable("uuid") uuid: String,
            @RequestBody request: TransactionRequest,
            @CurrentSecurityContext securityContext: SecurityContext
    ): Deposits {
        if (securityContext.authentication.name != uuid)
            throw OpexError.Forbidden.exception()
        return Deposits(depositPersister.findDepositHistory(
                uuid,
                request.coin,
                request.startTime?.let {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
                }
                        ?: null,
                request.endTime?.let {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
                } ?: null,
                request.limit!!,
                request.offset!!,
                request.ascendingByTime
        ).deposits.map {
            it.apply { it.createDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() }

        })
    }

    @PostMapping("/deposit/{amount}_{chain}_{symbol}/{receiverUuid}_{receiverWalletType}")
    @ApiResponse(
            message = "OK",
            code = 200,
            examples = Example(
                    ExampleProperty(
                            value = "{ }",
                            mediaType = "application/json"
                    )
            )
    )
    suspend fun deposit(
            @PathVariable("symbol") symbol: String,
            @PathVariable("receiverUuid") receiverUuid: String,
            @PathVariable("receiverWalletType") receiverWalletType: String,
            @PathVariable("amount") amount: BigDecimal,
            @RequestParam("description") description: String?,
            @RequestParam("transferRef") transferRef: String?,
            @RequestParam("chain") chain: String?
    ): TransferResult {
        return transferService.deposit(symbol, receiverUuid, receiverWalletType, amount, description, transferRef, chain)
    }


    @PostMapping("/manually/deposit/{amount}_{symbol}/{receiverUuid}")
    @ApiResponse(
            message = "OK",
            code = 200,
            examples = Example(
                    ExampleProperty(
                            value = "{ }",
                            mediaType = "application/json"
                    )
            )
    )
    suspend fun depositManually(
            @PathVariable("symbol") symbol: String,
            @PathVariable("receiverUuid") receiverUuid: String,
            @PathVariable("amount") amount: BigDecimal,
            @RequestBody request: ManualTransferRequest,
            @CurrentSecurityContext securityContext: SecurityContext
    ): TransferResult {

        return transferService.depositManually(symbol, receiverUuid,
                securityContext.authentication.name, amount, request)
    }


}



