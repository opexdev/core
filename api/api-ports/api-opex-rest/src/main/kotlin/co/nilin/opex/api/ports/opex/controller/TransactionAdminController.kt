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

    @PostMapping("/summary")
    suspend fun getUserTransactionHistory(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest,
    ): List<UserTransactionHistory> {
        return walletProxy.getUserTransactionHistoryForAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/deposits")
    suspend fun getDepositTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminSearchDepositRequest
    ): List<DepositAdminResponse> {
        return walletProxy.getDepositTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/withdraws")
    suspend fun getWithdrawTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminSearchWithdrawRequest
    ): List<WithdrawAdminResponse> {
        return walletProxy.getWithdrawTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/swaps")
    suspend fun getSwapTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest,
    ): List<AdminSwapResponse> {
        return walletProxy.getSwapTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    // This part is temporary and the structure of fetching trades needs to be fixed.
    @PostMapping("/trades")
    suspend fun getTransactionHistory(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminTradeHistoryRequest,
    ): List<TradeAdminResponse> {
        return walletProxy.getTradeHistoryForAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }
}