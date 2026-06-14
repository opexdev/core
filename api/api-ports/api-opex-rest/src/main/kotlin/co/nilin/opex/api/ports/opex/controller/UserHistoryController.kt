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
    description = """Authenticated user history, summaries, and swap-history operations."""
)
class UserHistoryController(
    private val marketUserDataProxy: MarketUserDataProxy,
    private val walletProxy: WalletProxy,
) {

    @GetMapping("/history/order")
    @Operation(
        summary = "Get order history",
        description = """GET /opex/v1/user/history/order.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` defaults to 10 and `offset` defaults to 0 when omitted.

Allowed values:
- orderType: LIMIT_ORDER, MARKET_ORDER.
- direction: ASK, BID.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = OrderDataResponse::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getOrderHistory(
        @Parameter(name = "symbol", description = "Optional trading pair symbol.", required = false)
        @RequestParam(name = "symbol", required = false) symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(
            name = "orderType",
            description = "Optional order type filter. Allowed values: LIMIT_ORDER, MARKET_ORDER.",
            required = false
        )
        @RequestParam(name = "orderType", required = false) orderType: MatchingOrderType?,
        @Parameter(
            name = "direction",
            description = "Optional order direction filter. Allowed values: ASK, BID.",
            required = false
        )
        @RequestParam(name = "direction", required = false) direction: OrderDirection?,
        @Parameter(name = "limit", description = "Optional page size. Defaults to 10 when omitted.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset. Defaults to 0 when omitted.", required = false)
        @RequestParam(name = "offset", required = false) offset: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<OrderDataResponse> {
        return marketUserDataProxy.getOrderHistory(
            securityContext.jwtAuthentication().tokenValue(),
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
    @Operation(
        summary = "Count order history",
        description = """GET /opex/v1/user/history/order/count.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.

Allowed values:
- orderType: LIMIT_ORDER, MARKET_ORDER.
- direction: ASK, BID.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Long::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getOrderHistoryCount(
        @Parameter(name = "symbol", description = "Optional trading pair symbol.", required = false)
        @RequestParam(name = "symbol", required = false) symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(
            name = "orderType",
            description = "Optional order type filter. Allowed values: LIMIT_ORDER, MARKET_ORDER.",
            required = false
        )
        @RequestParam(name = "orderType", required = false) orderType: MatchingOrderType?,
        @Parameter(
            name = "direction",
            description = "Optional order direction filter. Allowed values: ASK, BID.",
            required = false
        )
        @RequestParam(name = "direction", required = false) direction: OrderDirection?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return marketUserDataProxy.getOrderHistoryCount(
            securityContext.jwtAuthentication().tokenValue(),
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
        )
    }

    @GetMapping("/history/trade")
    @Operation(
        summary = "Get trade history",
        description = """GET /opex/v1/user/history/trade.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` defaults to 10 and `offset` defaults to 0 when omitted.

Allowed values:
- direction: ASK, BID.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = Trade::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTradeHistory(
        @Parameter(name = "symbol", description = "Optional trading pair symbol.", required = false)
        @RequestParam(name = "symbol", required = false) symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(
            name = "direction",
            description = "Optional trade direction filter. Allowed values: ASK, BID.",
            required = false
        )
        @RequestParam(name = "direction", required = false) direction: OrderDirection?,
        @Parameter(name = "limit", description = "Optional page size. Defaults to 10 when omitted.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset. Defaults to 0 when omitted.", required = false)
        @RequestParam(name = "offset", required = false) offset: Int?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Trade> {
        return marketUserDataProxy.getTradeHistory(
            securityContext.jwtAuthentication().tokenValue(),
            symbol,
            startTime,
            endTime,
            direction,
            limit ?: 10,
            offset ?: 0
        )
    }

    @GetMapping("/history/trade/count")
    @Operation(
        summary = "Count trade history",
        description = """GET /opex/v1/user/history/trade/count.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.

Allowed values:
- direction: ASK, BID.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Long::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTradeHistoryCount(
        @Parameter(name = "symbol", description = "Optional trading pair symbol.", required = false)
        @RequestParam(name = "symbol", required = false) symbol: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(
            name = "direction",
            description = "Optional trade direction filter. Allowed values: ASK, BID.",
            required = false
        )
        @RequestParam(name = "direction", required = false) direction: OrderDirection?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return marketUserDataProxy.getTradeHistoryCount(
            securityContext.jwtAuthentication().tokenValue(),
            symbol,
            startTime,
            endTime,
            direction
        )
    }

    @GetMapping("/history/withdraw")
    @Operation(
        summary = "Get withdraw history",
        description = """GET /opex/v1/user/history/withdraw.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` defaults to 10 and `offset` defaults to 0 when omitted.
- `ascendingByTime` controls time sorting when supported.

Allowed values:
- status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.
- transferMethod in response: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = WithdrawResponse::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getWithdrawHistory(
        @Parameter(name = "currency", description = "Optional currency symbol.", required = false)
        @RequestParam(name = "currency", required = false) currency: String?,
        @Parameter(
            name = "status",
            description = "Optional withdraw status. Allowed values: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.",
            required = false
        )
        @RequestParam(name = "status", required = false) status: WithdrawStatus?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size. Defaults to 10 when omitted.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset. Defaults to 0 when omitted.", required = false)
        @RequestParam(name = "offset", required = false) offset: Int?,
        @Parameter(
            name = "ascendingByTime",
            description = "Optional sorting flag. true sorts ascending by time; false sorts descending where supported.",
            required = false
        )
        @RequestParam(name = "ascendingByTime", required = false) ascendingByTime: Boolean?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
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
            ascendingByTime,
        )
    }

    @GetMapping("/history/withdraw/count")
    @Operation(
        summary = "Count withdraw history",
        description = """GET /opex/v1/user/history/withdraw/count.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.

Allowed values:
- status: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Long::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getWithdrawHistoryCount(
        @Parameter(name = "currency", description = "Optional currency symbol.", required = false)
        @RequestParam(name = "currency", required = false) currency: String?,
        @Parameter(
            name = "status",
            description = "Optional withdraw status. Allowed values: REQUESTED, CREATED, ACCEPTED, CANCELED, REJECTED, DONE.",
            required = false
        )
        @RequestParam(name = "status", required = false) status: WithdrawStatus?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Long {
        return walletProxy.getWithdrawTransactionsCount(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            currency,
            status,
            startTime,
            endTime,
        )
    }

    @GetMapping("/history/deposit")
    @Operation(
        summary = "Get deposit history",
        description = """GET /opex/v1/user/history/deposit.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` defaults to 10 and `offset` defaults to 0 when omitted.
- `ascendingByTime` controls time sorting when supported.

Allowed values in response:
- status: PROCESSING, DONE, INVALID.
- type: ON_CHAIN, OFF_CHAIN.
- transferMethod: CARD, SHEBA, IPG, EXCHANGE, MANUALLY, VOUCHER, MPG, REWARD.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = DepositHistoryResponse::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getDepositHistory(
        @Parameter(name = "currency", description = "Optional currency symbol.", required = false)
        @RequestParam(name = "currency", required = false) currency: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size. Defaults to 10 when omitted.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset. Defaults to 0 when omitted.", required = false)
        @RequestParam(name = "offset", required = false) offset: Int?,
        @Parameter(
            name = "ascendingByTime",
            description = "Optional sorting flag. true sorts ascending by time; false sorts descending where supported.",
            required = false
        )
        @RequestParam(name = "ascendingByTime", required = false) ascendingByTime: Boolean?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Count deposit history",
        description = """GET /opex/v1/user/history/deposit/count.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Long::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getDepositHistoryCount(
        @Parameter(name = "currency", description = "Optional currency symbol.", required = false)
        @RequestParam(name = "currency", required = false) currency: String?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Get transaction history",
        description = """GET /opex/v1/user/history/transaction.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` defaults to 10 and `offset` defaults to 0 when omitted.
- `ascendingByTime` controls time sorting when supported.

Allowed values:
- category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = UserTransactionHistory::class))
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTransactionHistory(
        @Parameter(name = "currency", description = "Optional currency symbol.", required = false)
        @RequestParam(name = "currency", required = false) currency: String?,
        @Parameter(
            name = "category",
            description = "Optional transaction category. Allowed values: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.",
            required = false
        )
        @RequestParam(name = "category", required = false) category: UserTransactionCategory?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(name = "limit", description = "Optional page size. Defaults to 10 when omitted.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(name = "offset", description = "Optional page offset. Defaults to 0 when omitted.", required = false)
        @RequestParam(name = "offset", required = false) offset: Int?,
        @Parameter(
            name = "ascendingByTime",
            description = "Optional sorting flag. true sorts ascending by time; false sorts descending where supported.",
            required = false
        )
        @RequestParam(name = "ascendingByTime", required = false) ascendingByTime: Boolean?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Count transaction history",
        description = """GET /opex/v1/user/history/transaction/count.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.

Allowed values:
- category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Long::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTransactionHistoryCount(
        @Parameter(name = "currency", description = "Optional currency symbol.", required = false)
        @RequestParam(name = "currency", required = false) currency: String?,
        @Parameter(
            name = "category",
            description = "Optional transaction category. Allowed values: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.",
            required = false
        )
        @RequestParam(name = "category", required = false) category: UserTransactionCategory?,
        @Parameter(
            name = "startTime",
            description = "Optional start timestamp in epoch milliseconds.",
            required = false
        )
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Get trade summary",
        description = """GET /opex/v1/user/summary/trade.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` limits the number of summary rows when supported.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = TransactionSummary::class))
                    )
                ]
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
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(name = "limit", description = "Optional number of summary rows.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Get deposit summary",
        description = """GET /opex/v1/user/summary/deposit.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` limits the number of summary rows when supported.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = TransactionSummary::class))
                    )
                ]
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
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(name = "limit", description = "Optional number of summary rows.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Get withdraw summary",
        description = """GET /opex/v1/user/summary/withdraw.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` limits the number of summary rows when supported.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = TransactionSummary::class))
                    )
                ]
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
        @RequestParam(name = "startTime", required = false) startTime: Long?,
        @Parameter(name = "endTime", description = "Optional end timestamp in epoch milliseconds.", required = false)
        @RequestParam(name = "endTime", required = false) endTime: Long?,
        @Parameter(name = "limit", description = "Optional number of summary rows.", required = false)
        @RequestParam(name = "limit", required = false) limit: Int?,
        @Parameter(hidden = true)
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
    @Operation(
        summary = "Get swap history",
        description = """POST /opex/v1/user/history/swap.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.
- `limit` defaults to 10 and `offset` defaults to 0 when omitted.

Allowed values:
- status: Created, Expired, Committed.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserSwapTransactionRequest::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = SwapResponse::class))
                    )
                ]
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
        summary = "Count swap history",
        description = """POST /opex/v1/user/history/swap/count.
Security: Bearer user-token required. Requires authenticated user JWT.

Behavior / Validation:
- Optional filters are applied only when provided.
- `startTime` and `endTime` are epoch milliseconds.

Allowed values:
- status: Created, Expired, Committed.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserSwapTransactionRequest::class)
            )]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Long::class))]
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
