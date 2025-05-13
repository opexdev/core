package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController()
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
        @RequestParam limit: Int? = 10,
        @RequestParam offset: Int? = 0,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<OrderData> {
        return marketUserDataProxy.getOrderHistory(
            securityContext.authentication.name,
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
            limit,
            offset,
        )
    }

    @GetMapping("/history/trade")
    suspend fun getTradeHistory(
        @RequestParam symbol: String?,
        @RequestParam startTime: Long?,
        @RequestParam endTime: Long?,
        @RequestParam direction: OrderDirection?,
        @RequestParam limit: Int? = 10,
        @RequestParam offset: Int? = 0,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade> {
        return marketUserDataProxy.getTradeHistory(
            securityContext.authentication.name, symbol, startTime, endTime, direction, limit, offset
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
            securityContext.authentication.name,
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
        return walletProxy.getUserDesitSummary(
            securityContext.authentication.name,
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
            securityContext.authentication.name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit,
        )
    }
}