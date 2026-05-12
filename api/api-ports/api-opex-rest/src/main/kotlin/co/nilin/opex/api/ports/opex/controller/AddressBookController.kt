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
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/address-book")
@Tag(
    name = "Address Book",
    description = "Manage authenticated user's saved destination addresses."
)
class AddressBookController(
    val profileProxy: ProfileProxy,
) {

    @PostMapping
    @Operation(
        summary = "Create address book entry",
        description = """
Creates a new address book entry for the authenticated user.

Security:
- Bearer user-token is required.

Validation:
- `name`, `address`, and `addressType` are required.
- `addressType` is a server-provided string related to the selected chain/address type.
- Clients should use values returned by server APIs and should not hardcode a fixed enum list.

Request body: AddAddressBookItemRequest
Response 200: AddressBookResponse
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Address book entry creation payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AddAddressBookItemRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Create address book entry",
                            value = """
{
  "name": "Home Wallet",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "addressType": "ethereum"
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
                description = "Address book entry created successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AddressBookResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Address book response",
                                value = """
{
  "id": 1,
  "name": "Home Wallet",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "addressType": "ethereum"
}
                                """
                            )
                        ]
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
    suspend fun addAddressBook(
        @RequestBody request: AddAddressBookItemRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return profileProxy.addAddressBook(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @GetMapping
    @Operation(
        summary = "List address book entries",
        description = """
Returns all address book entries for the authenticated user.

Security:
- Bearer user-token is required.

Response 200: Array<AddressBookResponse>
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Address book entries returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = AddressBookResponse::class)),
                        examples = [
                            ExampleObject(
                                name = "Address book list response",
                                value = """
[
  {
    "id": 1,
    "name": "Home Wallet",
    "address": "0x1234567890abcdef1234567890abcdef12345678",
    "addressType": "ethereum"
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
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getAddressBook(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<AddressBookResponse> {
        return profileProxy.getAllAddressBooks(securityContext.jwtAuthentication().tokenValue())
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete address book entry",
        description = """
Deletes one address book entry by ID for the authenticated user.

Security:
- Bearer user-token is required.

Path parameters:
- `id`: address book entry ID.

Response 200:
- No response body.
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "id",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Address book entry ID.",
                schema = Schema(type = "integer", format = "int64"),
                example = "1"
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Address book entry deleted successfully. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun deleteAddressBook(
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.deleteAddressBook(securityContext.jwtAuthentication().tokenValue(), id)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update address book entry",
        description = """
Updates one address book entry by ID for the authenticated user.

Security:
- Bearer user-token is required.

Validation:
- `name`, `address`, and `addressType` are required.
- `addressType` is a server-provided string related to the selected chain/address type.
- Clients should use values returned by server APIs and should not hardcode a fixed enum list.

Path parameters:
- `id`: address book entry ID.

Request body: AddAddressBookItemRequest
Response 200: AddressBookResponse
        """,
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "id",
                `in` = ParameterIn.PATH,
                required = true,
                description = "Address book entry ID.",
                schema = Schema(type = "integer", format = "int64"),
                example = "1"
            )
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Address book entry update payload.",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = AddAddressBookItemRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Update address book entry",
                            value = """
{
  "name": "Updated Wallet Name",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "addressType": "ethereum"
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
                description = "Address book entry updated successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AddressBookResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Address book response",
                                value = """
{
  "id": 1,
  "name": "Updated Wallet Name",
  "address": "0x1234567890abcdef1234567890abcdef12345678",
  "addressType": "ethereum"
}
                                """
                            )
                        ]
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
    suspend fun updateAddressBook(
        @PathVariable("id") id: Long,
        @RequestBody request: AddAddressBookItemRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressBookResponse {
        return profileProxy.updateAddressBook(securityContext.jwtAuthentication().tokenValue(), id, request)
    }
}
