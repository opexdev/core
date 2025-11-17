package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/opex/v1/admin/transactions")
class TransactionAdminController(
    private val walletProxy: WalletProxy,
) {

    @PostMapping("/deposits")
    suspend fun getDepositTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminSearchDepositRequest
    ): List<DepositHistoryResponse> {
        return walletProxy.getDepositTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/withdraws")
    suspend fun getWithdrawTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminSearchWithdrawRequest
    ): List<WithdrawHistoryResponse> {
        return walletProxy.getWithdrawTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/swaps")
    suspend fun getSwapTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest,
    ): List<SwapHistoryResponse> {
        return walletProxy.getSwapTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/history")
    suspend fun getTransactionsHistory(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminTransactionHistoryRequest,
    ): List<AdminTransactionHistory> {
        return walletProxy.getTransactionHistoryForAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/history/user")
    suspend fun getUserTransactionsHistory(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest,
    ): List<UserTransactionHistory> {
        return walletProxy.getUserTransactionHistoryForAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }
}