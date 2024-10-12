package co.nilin.opex.wallet.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.*
import co.nilin.opex.wallet.app.service.DepositService
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.service.WithdrawService
import co.nilin.opex.wallet.core.spi.DepositPersister
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.Example
import io.swagger.annotations.ExampleProperty
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/v1/deposit")
class DepositController(
    private val depositService: DepositService,
) {

    @PostMapping("/history")
    suspend fun getDepositTransactionsForUser(
            @RequestBody request: DepositHistoryRequest,
            @CurrentSecurityContext securityContext: SecurityContext
    ): List<DepositResponse> {
        return depositService.findDepositHistory(
            securityContext.authentication.name,
            request.currency,
            request.startTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.startTime), ZoneId.systemDefault())
            },
            request.endTime?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.endTime), ZoneId.systemDefault())
            },
            request.limit,
            request.offset,
            request.ascendingByTime
        ).map {
            DepositResponse(
                it.id!!,
                it.ownerUuid,
                it.currency,
                it.amount,
                it.network,
                it.note,
                it.transactionRef,
                it.status,
                it.depositType,
                it.createDate
            )
        }
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
            @PathVariable symbol: String,
            @PathVariable receiverUuid: String,
            @PathVariable receiverWalletType: WalletType,
            @PathVariable amount: BigDecimal,
            @RequestParam description: String?,
            @RequestParam transferRef: String?,
            @PathVariable chain: String?
    ): TransferResult {
        return depositService.deposit(
                symbol,
                receiverUuid,
                receiverWalletType,
                amount,
                description,
                transferRef,
                chain
        )
    }


}



