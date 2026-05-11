package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddBankAccountRequest
import co.nilin.opex.api.core.inout.BankAccountResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/bank-account")
@Tag(
    name = "Bank Account",
    description = "Manage authenticated user's bank accounts."
)
class BankAccountController(
    val profileProxy: ProfileProxy,
) {

    @PostMapping
    @Operation(
        summary = "Add bank account",
        description = """
Creates a new bank account for the authenticated user.

Required authentication:
- Bearer JWT
- Required authority: PERM_bank_account:write

Request body: AddBankAccountRequest
- name: string, nullable
- cardNumber: string, nullable
- iban: string, nullable


Response body: BankAccountResponse
- id: Long, nullable
- name: string, nullable
- cardNumber: string, nullable
- iban: string, nullable
- accountNumber: string, nullable
- bank: string, nullable
- status: WAITING | VERIFIED | REJECTED
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Bank account creation payload, Exactly one of `cardNumber` or `iban` must be provided. Providing both or neither is invalid.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AddBankAccountRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Add bank account request",
                            value = """
{
  "name": "My Bank Account",
  "cardNumber": "6037991234567890",
  "iban": null
}
                            """
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Bank account created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BankAccountResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Bank account response",
                                value = """
{
  "id": 1,
  "name": "My Bank Account",
  "cardNumber": "6037991234567890",
  "iban": "IR123456789012345678901234",
  "accountNumber": null,
  "bank": null,
  "status": "WAITING"
}
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]

            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: PERM_bank_account:write.",
                content = [Content()]
            )
        ]
    )
    suspend fun addBankAccount(
        @RequestBody request: AddBankAccountRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountResponse {
        return profileProxy.addBankAccount(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    @Operation(
        summary = "List bank accounts",
        description = """
Returns bank accounts for the authenticated user.

Required authentication:
- Bearer JWT

Response body: Array<BankAccountResponse>
Each item:
- id: Long, nullable
- name: string, nullable
- cardNumber: string, nullable
- iban: string, nullable
- accountNumber: string, nullable
- bank: string, nullable
- status: WAITING | VERIFIED | REJECTED
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Bank accounts returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(
                            schema = Schema(implementation = BankAccountResponse::class)
                        ),
                        examples = [
                            ExampleObject(
                                name = "Bank account list response",
                                value = """
[
  {
    "id": 1,
    "name": "My Bank Account",
    "cardNumber": "6037991234567890",
    "iban": "IR123456789012345678901234",
    "accountNumber": null,
    "bank": null,
    "status": "WAITING"
  }
]
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            )
        ]
    )
    suspend fun getBankAccounts(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<BankAccountResponse> {
        return profileProxy.getBankAccounts(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete bank account",
        description = """
Deletes one bank account by ID for the authenticated user.

Required authentication:
- Bearer JWT
- Required authority: PERM_bank_account:write

Path parameters:
- id: Bank account ID.

Response body:
- No response body.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "id",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Bank account ID.",
                schema = Schema(type = "integer", format = "int64"),
                example = "1"
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Bank account deleted successfully. No response body."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: PERM_bank_account:write.",
                content = [Content()]
            )
        ]
    )
    suspend fun deleteBankAccount(
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteBankAccount(securityContext.jwtAuthentication().tokenValue(), id)
    }
}