package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddAddressBookItemRequest
import co.nilin.opex.api.core.inout.AddressBookResponse
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
@RequestMapping("/opex/v1/address-book")
@Tag(name = "Address Book", description = "Manage user address book entries")
class AddressBookController(
    val profileProxy: ProfileProxy,
) {

    @PostMapping
    @Operation(
        summary = "Add address book entry",
        description = "Creates a new address book entry for the authenticated user.",
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AddAddressBookItemRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Created/Updated",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AddressBookResponse::class))]
            )
        ]
    )
    suspend fun addAddressBook(
        @RequestBody request: AddAddressBookItemRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return profileProxy.addAddressBook(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    @Operation(
        summary = "List address book entries",
        description = "Returns all address book entries for the authenticated user.",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = AddressBookResponse::class)))]
            )
        ]
    )
    suspend fun getAddressBook(@CurrentSecurityContext securityContext: SecurityContext): List<AddressBookResponse> {
        return profileProxy.getAllAddressBooks(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete address book entry",
        description = "Deletes an address book entry by id.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "id", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "integer", format = "int64"))
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "Deleted")
        ]
    )
    suspend fun deleteAddressBook(
        @PathVariable("id") id: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteAddressBook(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update address book entry",
        description = "Updates an existing address book entry by id.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "id", `in` = ParameterIn.PATH, required = true, schema = Schema(type = "integer", format = "int64"))
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AddAddressBookItemRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AddressBookResponse::class))]
            )
        ]
    )
    suspend fun updateAddressBook(
        @PathVariable("id") id: Long,
        @RequestBody request: AddAddressBookItemRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return profileProxy.updateAddressBook(securityContext.jwtAuthentication().tokenValue(), id, request)
    }

}