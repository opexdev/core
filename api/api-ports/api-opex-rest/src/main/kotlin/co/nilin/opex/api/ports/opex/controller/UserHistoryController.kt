package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.data.OrderDataResponse
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.toResponse
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/user")
class UserHistoryController(
    private val marketUserDataProxy: MarketUserDataProxy,
    private val walletProxy: WalletProxy,
) {

    @GetMapping("/history/order")
    suspend fun getOrderHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam orderType: MatchingOrderType?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<OrderDataResponse> {
        return marketUserDataProxy.getOrderHistory(
            securityContext.authentication.name,
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
            limit ?: 10,
            offset ?: 0,
        ).map { it.toResponse() }
    }

    @GetMapping("/history/order/count")
    suspend fun getOrderHistoryCount(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam orderType: MatchingOrderType?,
        @RequestParam direction: OrderDirection?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return marketUserDataProxy.getOrderHistoryCount(
            securityContext.authentication.name,
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
        )
    }

    @GetMapping("/history/trade")
    suspend fun getTradeHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade> {
        return marketUserDataProxy.getTradeHistory(
            securityContext.authentication.name, symbol, startTime, endTime, direction, limit ?: 10, offset ?: 0
        )
    }

    @GetMapping("/history/trade/count")
    suspend fun getTradeHistoryCount(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam direction: OrderDirection?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return marketUserDataProxy.getTradeHistoryCount(
            securityContext.authentication.name, symbol, startTime, endTime, direction
        )
    }

    @GetMapping("/history/withdraw")
    suspend fun getWithdrawHistory(
        @RequestParam currency: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestParam ascendingByTime: Boolean?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<WithdrawResponse> {
        return walletProxy.getWithdrawTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            startTime,
            endTime,
            limit ?: 10,
            offset ?: 0,
            ascendingByTime,
        )
    }

    @GetMapping("/history/withdraw/count")
    suspend fun getWithdrawHistoryCount(
        @RequestParam currency: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return walletProxy.getWithdrawTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            startTime,
            endTime,
        )
    }

    @GetMapping("/history/deposit")
    suspend fun getDepositHistory(
        @RequestParam currency: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestParam ascendingByTime: Boolean?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<DepositHistoryResponse> {
        return walletProxy.getDepositTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            startTime,
            endTime,
            limit ?: 10,
            offset ?: 0,
            ascendingByTime,
        )
    }

    @GetMapping("/history/deposit/count")
    suspend fun getDepositHistoryCount(
        @RequestParam currency: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return walletProxy.getDepositTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            startTime,
            endTime,
        )
    }

    @GetMapping("/history/transaction")
    suspend fun getTransactionHistory(
        @RequestParam currency: String?,
        @RequestParam category: UserTransactionCategory?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestParam ascendingByTime: Boolean?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<UserTransactionHistory> {
        return walletProxy.getTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            category,
            startTime,
            endTime,
            limit ?: 10,
            offset ?: 0,
            ascendingByTime,
        )
    }

    @GetMapping("/history/transaction/count")
    suspend fun getTransactionHistoryCount(
        @RequestParam currency: String?,
        @RequestParam category: UserTransactionCategory?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return walletProxy.getTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            category,
            startTime,
            endTime,
        )
    }

    @GetMapping("/summary/trade")
    suspend fun getTradeTransactionSummary(
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<TransactionSummary> {
        return walletProxy.getUserTradeTransactionSummary(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit,
        )
    }

    @GetMapping("/summary/deposit")
    suspend fun getDepositSummary(
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<TransactionSummary> {
        return walletProxy.getUserDepositSummary(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit,
        )
    }

    @GetMapping("/summary/withdraw")
    suspend fun getWithdrawSummary(
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam limit: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<TransactionSummary> {
        return walletProxy.getUserWithdrawSummary(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit,
        )
    }

    @PostMapping("/history/swap")
    suspend fun getSwapHistory(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest
    ): List<SwapResponse> {
        return walletProxy.getSwapTransactions(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/history/swap/count")
    suspend fun getSwapHistoryCount(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest
    ): Long {
        return walletProxy.getSwapTransactionsCount(securityContext.jwtAuthentication().tokenValue(), request)
    }
}