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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag


@RestController
@RequestMapping("/opex/v1/admin/transactions")
@Tag(name = "Transactions Admin", description = "Admin transaction history and summary operations.")
class TransactionAdminController(
    private val walletProxy: WalletProxy
) {

    @PostMapping("/summary")
    @Operation(
        summary = "Get user transaction history",
        description = """POST /opex/v1/admin/transactions/summary.
Security: Bearer admin-token required. Required authority: ROLE_monitoring or ROLE_admin.
Allowed values:
- category: TRADE, DEPOSIT, DEPOSIT_TO, WITHDRAW_FROM, WITHDRAW, FEE, SWAP, REFERRAL_COMMISSION, REFERRAL_KYC_REWARD, REFERENT_COMMISSION, KYC_ACCEPTED_REWARD, SYSTEM.

""",

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
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_monitoring or ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
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
}
