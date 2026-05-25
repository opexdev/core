package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.opex.data.OrderDataResponse
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.toResponse
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/user")
@Tag(
    name = "User History",
    description = "Authenticated user order, trade, deposit, withdraw, transaction, and swap history operations."
)
class UserHistoryController(
    private val marketUserDataProxy: MarketUserDataProxy,
    private val walletProxy: WalletProxy
) {

    @GetMapping("/history/order")
    @Operation(
        summary = "Get order history",
        description = """GET /opex/v1/user/history/order.
Behavior: Optional filters include symbol, time range, orderType, direction, limit, and offset. `orderType`: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER. `direction`: BUY, SELL.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = OrderDataResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getOrderHistory(
        @Parameter(
            name = "symbol",
            description = "Trading pair or currency symbol, depending on endpoint.",
            required = false
        )
        @RequestParam symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(
            name = "orderType",
            description = "Order type. Allowed values: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.",
            required = false
        )
        @RequestParam orderType: MatchingOrderType?,
        @Parameter(
            name = "direction",
            description = "Order/trade direction. Allowed values: BUY, SELL.",
            required = false
        )
        @RequestParam direction: OrderDirection?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<OrderDataResponse> {
        return marketUserDataProxy.getOrderHistory(
            securityContext.authentication.name,
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
            limit ?: 10,
            offset ?: 0
        ).map { it.toResponse() }
    }

    @GetMapping("/history/order/count")
    @Operation(
        summary = "Get order history count",
        description = """GET /opex/v1/user/history/order/count.
Behavior: Same filters as order history, but returns count only. `orderType`: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER. `direction`: BUY, SELL.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "integer", format = "int64"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getOrderHistoryCount(
        @Parameter(
            name = "symbol",
            description = "Trading pair or currency symbol, depending on endpoint.",
            required = false
        )
        @RequestParam symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(
            name = "orderType",
            description = "Order type. Allowed values: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.",
            required = false
        )
        @RequestParam orderType: MatchingOrderType?,
        @Parameter(
            name = "direction",
            description = "Order/trade direction. Allowed values: BUY, SELL.",
            required = false
        )
        @RequestParam direction: OrderDirection?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Long {
        return marketUserDataProxy.getOrderHistoryCount(
            securityContext.authentication.name,
            symbol,
            startTime,
            endTime,
            orderType,
            direction
        )
    }

    @GetMapping("/history/trade")
    @Operation(
        summary = "Get trade history",
        description = """GET /opex/v1/user/history/trade.
Behavior: Optional filters include symbol, time range, direction, limit, and offset. `direction`: BUY, SELL.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = Trade::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTradeHistory(
        @Parameter(
            name = "symbol",
            description = "Trading pair or currency symbol, depending on endpoint.",
            required = false
        )
        @RequestParam symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(
            name = "direction",
            description = "Order/trade direction. Allowed values: BUY, SELL.",
            required = false
        )
        @RequestParam direction: OrderDirection?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<Trade> {
        return marketUserDataProxy.getTradeHistory(
            securityContext.authentication.name, symbol, startTime, endTime, direction, limit ?: 10, offset ?: 0
        )
    }

    @GetMapping("/history/trade/count")
    @Operation(
        summary = "Get trade history count",
        description = """GET /opex/v1/user/history/trade/count.
Behavior: Same filters as trade history, but returns count only. `direction`: BUY, SELL.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "integer", format = "int64"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTradeHistoryCount(
        @Parameter(
            name = "symbol",
            description = "Trading pair or currency symbol, depending on endpoint.",
            required = false
        )
        @RequestParam symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(
            name = "direction",
            description = "Order/trade direction. Allowed values: BUY, SELL.",
            required = false
        )
        @RequestParam direction: OrderDirection?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Long {
        return marketUserDataProxy.getTradeHistoryCount(
            securityContext.authentication.name, symbol, startTime, endTime, direction
        )
    }

    @GetMapping("/history/withdraw")
    @Operation(
        summary = "Get withdraw history",
        description = """GET /opex/v1/user/history/withdraw.
Behavior: Optional filters include currency, status, time range, pagination, and sorting. `status`: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = WithdrawResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getWithdrawHistory(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam currency: String?,
        @Parameter(
            name = "status",
            description = "Withdraw status. Allowed values: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.",
            required = false
        )
        @RequestParam status: WithdrawStatus?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?,
        @Parameter(
            name = "ascendingByTime",
            description = "Optional sorting flag. true sorts ascending by time; false sorts descending where supported.",
            required = false
        )
        @RequestParam ascendingByTime: Boolean?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<WithdrawResponse> {
        return walletProxy.getWithdrawTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            status,
            startTime,
            endTime,
            limit ?: 10,
            offset ?: 0,
            ascendingByTime
        )
    }

    @GetMapping("/history/withdraw/count")
    @Operation(
        summary = "Get withdraw history count",
        description = """GET /opex/v1/user/history/withdraw/count.
Behavior: Same filters as withdraw history, but returns count only. `status`: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "integer", format = "int64"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getWithdrawHistoryCount(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam currency: String?,
        @Parameter(
            name = "status",
            description = "Withdraw status. Allowed values: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.",
            required = false
        )
        @RequestParam status: WithdrawStatus?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Long {
        return walletProxy.getWithdrawTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            status,
            startTime,
            endTime
        )
    }

    @GetMapping("/history/deposit")
    @Operation(
        summary = "Get deposit history",
        description = """GET /opex/v1/user/history/deposit.
Behavior: Optional filters include currency, time range, pagination, and sorting.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = DepositHistoryResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getDepositHistory(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam currency: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?,
        @Parameter(
            name = "ascendingByTime",
            description = "Optional sorting flag. true sorts ascending by time; false sorts descending where supported.",
            required = false
        )
        @RequestParam ascendingByTime: Boolean?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<DepositHistoryResponse> {
        return walletProxy.getDepositTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            startTime,
            endTime,
            limit ?: 10,
            offset ?: 0,
            ascendingByTime
        )
    }

    @GetMapping("/history/deposit/count")
    @Operation(
        summary = "Get deposit history count",
        description = """GET /opex/v1/user/history/deposit/count.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "integer", format = "int64"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getDepositHistoryCount(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam currency: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Long {
        return walletProxy.getDepositTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            startTime,
            endTime
        )
    }

    @GetMapping("/history/transaction")
    @Operation(
        summary = "Get transaction history",
        description = """GET /opex/v1/user/history/transaction.
Behavior: Optional filters include currency, category, time range, pagination, and sorting. `category`: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = UserTransactionHistory::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTransactionHistory(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam currency: String?,
        @Parameter(
            name = "category",
            description = "Transaction category. Allowed values: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.",
            required = false
        )
        @RequestParam category: UserTransactionCategory?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset.", required = false)
        @RequestParam offset: Int?,
        @Parameter(
            name = "ascendingByTime",
            description = "Optional sorting flag. true sorts ascending by time; false sorts descending where supported.",
            required = false
        )
        @RequestParam ascendingByTime: Boolean?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
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
            ascendingByTime
        )
    }

    @GetMapping("/history/transaction/count")
    @Operation(
        summary = "Get transaction history count",
        description = """GET /opex/v1/user/history/transaction/count.
Behavior: Same filters as transaction history, but returns count only. `category`: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "integer", format = "int64"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTransactionHistoryCount(
        @Parameter(name = "currency", description = "Currency symbol, e.g. USDT.", required = false)
        @RequestParam currency: String?,
        @Parameter(
            name = "category",
            description = "Transaction category. Allowed values: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.",
            required = false
        )
        @RequestParam category: UserTransactionCategory?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): Long {
        return walletProxy.getTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            category,
            startTime,
            endTime
        )
    }

    @GetMapping("/summary/trade")
    @Operation(
        summary = "Get trade transaction summary",
        description = """GET /opex/v1/user/summary/trade.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = TransactionSummary::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTradeTransactionSummary(
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<TransactionSummary> {
        return walletProxy.getUserTradeTransactionSummary(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit
        )
    }

    @GetMapping("/summary/deposit")
    @Operation(
        summary = "Get deposit summary",
        description = """GET /opex/v1/user/summary/deposit.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = TransactionSummary::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getDepositSummary(
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<TransactionSummary> {
        return walletProxy.getUserDepositSummary(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit
        )
    }

    @GetMapping("/summary/withdraw")
    @Operation(
        summary = "Get withdraw summary",
        description = """GET /opex/v1/user/summary/withdraw.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = TransactionSummary::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getWithdrawSummary(
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size.", required = false)
        @RequestParam limit: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<TransactionSummary> {
        return walletProxy.getUserWithdrawSummary(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            startTime,
            endTime,
            limit
        )
    }

    @PostMapping("/history/swap")
    @Operation(
        summary = "Get swap history",
        description = """POST /opex/v1/user/history/swap.
Behavior: Request body contains swap history filters. Pagination and time range are handled by the request schema.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = SwapResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getSwapHistory(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserSwapTransactionRequest
    ): List<SwapResponse> {
        return walletProxy.getSwapTransactions(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/history/swap/count")
    @Operation(
        summary = "Get swap history count",
        description = """POST /opex/v1/user/history/swap/count.
Behavior: Same filter body as swap history, but returns count only.
Security: Bearer user-token required. Requires authenticated user JWT.
Allowed values:
- orderType: LIMIT, MARKET, STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, TAKE_PROFIT_LIMIT, LIMIT_MAKER.
- direction: BUY, SELL.
- withdraw status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transaction category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(type = "integer", format = "int64"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getSwapHistoryCount(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserSwapTransactionRequest
    ): Long {
        return walletProxy.getSwapTransactionsCount(securityContext.jwtAuthentication().tokenValue(), request)
    }
}
