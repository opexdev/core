package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.core.spi.MarketUserDataProxy
import co.nilin.opex.api.ports.opex.service.OwnerNameResolver
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/opex/v1/admin/transactions")
@Tag(name = "Transactions Admin", description = "Admin transaction history and summary operations.")
class TransactionAdminController(
    private val walletProxy: WalletProxy,
    private val marketDataProxy: MarketDataProxy,
    private val marketUserDataProxy: MarketUserDataProxy,
    private val ownerNameResolver: OwnerNameResolver,
) {

    @PostMapping("/summary")
    suspend fun getUserTransactionHistory(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserTransactionRequest
    ): List<UserTransactionHistory> {
        return walletProxy.getUserTransactionHistoryForAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/deposits")
    @Operation(
        summary = "Get deposit transactions",
        description = """POST /opex/v1/admin/transactions/deposits.
Security: Bearer admin-token required. Required authority: ROLE_monitoring or ROLE_admin.
Allowed values:
- DepositStatus: PROCESSING, DONE, INVALID
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = DepositAdminResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_monitoring or ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getDepositTransactions(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminDepositHistoryRequest
    ): List<DepositAdminResponse> {
        return walletProxy.getDepositTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/withdraws")
    @Operation(
        summary = "Get withdraw transactions",
        description = """POST /opex/v1/admin/transactions/withdraws.
Security: Bearer admin-token required. Required authority: ROLE_monitoring or ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = WithdrawAdminResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_monitoring or ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getWithdrawTransactions(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminWithdrawHistoryRequest
    ): List<WithdrawAdminResponse> {
        return walletProxy.getWithdrawTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @PostMapping("/swaps")
    @Operation(
        summary = "Get swap transactions",
        description = """POST /opex/v1/admin/transactions/swaps.
Security: Bearer admin-token required. Required authority: ROLE_monitoring or ROLE_admin.
Allowed values:
- ReservedStatus: Created, Expired, Committed,
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = SwapAdminResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_monitoring or ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getSwapTransactions(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: UserSwapTransactionRequest
    ): List<SwapAdminResponse> {
        return walletProxy.getSwapTransactionsForAdmin(
            securityContext.jwtAuthentication().tokenValue(),
            request
        )
    }

    @Deprecated("Use /v2/trades instead")
    // This part is temporary and the structure of fetching trades needs to be fixed.
    @PostMapping("/trades")
    @Operation(
        summary = "Get transaction history",
        description = """POST /opex/v1/admin/transactions/trades.
Security: Bearer admin-token required. Required authority: ROLE_monitoring or ROLE_admin.
""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = TradeAdminResponse::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_monitoring or ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getTransactionHistory(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminTradeHistoryRequest
    ): List<TradeAdminResponse> {
        return walletProxy.getTradeHistoryForAdmin(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PostMapping("/market-trades/search")
    suspend fun searchMarketTrades(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminTradesHistoryRequest,
    ): List<TradeAdminItem> {
        val token = securityContext.jwtAuthentication().tokenValue()
        val items = marketDataProxy.recentTrades(token, request)
        if (!request.includeNames) return items
        val uuids = items.flatMap { listOfNotNull(it.makerUuid, it.takerUuid) }.toSet()
        val nameMap = ownerNameResolver.resolve(token, uuids)
        return items.map { it.copy(
            makerOwnerName = nameMap[it.makerUuid],
            takerOwnerName = nameMap[it.takerUuid]
        ) }
    }

    @PostMapping("/market-orders/search")
    suspend fun searchMarketOrders(
        @CurrentSecurityContext securityContext: SecurityContext,
        @RequestBody request: AdminOrdersSearchRequest,
    ): List<OrderAdminItem> {
        val token = securityContext.jwtAuthentication().tokenValue()
        val orders = marketUserDataProxy.getOrderHistory(
            uuid = request.creatorUuid,
            symbol = request.symbol,
            startTime = request.startTime,
            endTime = request.endTime,
            orderType = request.orderType,
            direction = request.direction,
            limit = request.limit,
            offset = request.offset,
        )
        val items = orders.map { od ->
            OrderAdminItem(
                symbol = od.symbol,
                orderId = od.orderId,
                orderType = od.orderType,
                side = od.side,
                price = od.price,
                quantity = od.quantity,
                quoteQuantity = od.quoteQuantity,
                executedQuantity = od.executedQuantity,
                takerFee = od.takerFee,
                makerFee = od.makerFee,
                status = od.status,
                appearance = od.appearance,
                createDate = od.createDate,
                updateDate = od.updateDate,
                creatorUuid = request.creatorUuid,
                creatorOwnerName = null,
            )
        }
        if (!request.includeNames) return items
        val nameMap = ownerNameResolver.resolve(token, setOf(request.creatorUuid))
        val name = nameMap[request.creatorUuid]
        return items.map { it.copy(creatorOwnerName = name) }
    }
}
