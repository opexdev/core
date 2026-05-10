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
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/bank-account")
@Tag(name = "Bank Account", description = "Manage user bank accounts")
class BankAccountController(
    val profileProxy: ProfileProxy,
) {

    @PostMapping
    @Operation(
        summary = "Add bank account",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AddBankAccountRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", schema = Schema(implementation = BankAccountResponse::class))
            ])
        ]
    )
    suspend fun addBankAccount(
        @RequestBody request: AddBankAccountRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): BankAccountResponse {
        return profileProxy.addBankAccount(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    @Operation(
        summary = "List bank accounts",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(responseCode = "200", description = "OK", content = [
                Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = BankAccountResponse::class)))
            ])
        ]
    )
    suspend fun getBankAccounts(@CurrentSecurityContext securityContext: SecurityContext): List<BankAccountResponse> {
        return profileProxy.getBankAccounts(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete bank account",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "id", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "integer", format = "int64"))
        ],
        responses = [ ApiResponse(responseCode = "200", description = "Deleted") ]
    )
    suspend fun deleteBankAccount(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteBankAccount(securityContext.jwtAuthentication().tokenValue(), id)
    }


}