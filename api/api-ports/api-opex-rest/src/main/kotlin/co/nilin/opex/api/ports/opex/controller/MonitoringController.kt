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
@RequestMapping("/opex/v1/monitoring")
class MonitoringController(
    private val walletProxy: WalletProxy,
) {

    @PostMapping("/deposits")
    suspend fun getDepositTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: InquiryRequest,
    ): List<DepositHistoryResponse> {
        return walletProxy.getDepositTransactionsForMonitoring(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/withdraws")
    suspend fun getWithdrawTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: InquiryRequest,
    ): List<WithdrawHistoryResponse> {
        return walletProxy.getWithdrawTransactionsForMonitoring(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/swaps")
    suspend fun getSwapTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: InquiryRequest,
    ): List<SwapHistoryResponse> {
        return walletProxy.getSwapTransactionsForMonitoring(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/trades")
    suspend fun getTradeTransactions(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: InquiryRequest,
    ): List<AdminTransactionHistory> {
        return walletProxy.getRecentTradesForMonitoring(securityContext.jwtAuthentication().tokenValue(), request)
    }
}