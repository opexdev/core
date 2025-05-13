package co.nilin.opex.wallet.app.controller


import co.nilin.opex.wallet.app.dto.DepositHistoryRequest
import co.nilin.opex.wallet.app.service.DepositService
import co.nilin.opex.wallet.app.utils.asLocalDateTime
import co.nilin.opex.wallet.core.inout.DepositResponse
import co.nilin.opex.wallet.core.inout.TransactionSummary
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.DepositType
import co.nilin.opex.wallet.core.model.WalletType
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
@RequestMapping
class DepositController(
    private val depositService: DepositService,
) {

    @PostMapping("/v1/deposit/history")
    suspend fun getDepositTransactionsForUser(
        @RequestBody request: DepositHistoryRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
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
        )
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
        @RequestParam gatewayUuid: String?,
        @PathVariable chain: String?,
    ): TransferResult? {
        return depositService.deposit(
            symbol,
            receiverUuid,
            receiverWalletType,
            amount,
            description,
            transferRef,
            chain,
            null,
            depositType = DepositType.ON_CHAIN,
            gatewayUuid = gatewayUuid,
            null
        )
    }

    @GetMapping("/v1/deposit/summary/{uuid}")
    suspend fun getUserDepositSummary(
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @PathVariable uuid: String,
    ): List<TransactionSummary> {
        return depositService.getDepositSummary(
            uuid,
            startTime?.asLocalDateTime(),
            endTime?.asLocalDateTime(),
            limit,
        )
    }
}



