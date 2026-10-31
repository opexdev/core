package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.AddressAssignmentFilter
import co.nilin.opex.api.core.inout.AddressAssignmentPage
import co.nilin.opex.api.core.inout.AddressAssignmentRecord
import co.nilin.opex.api.core.inout.AdminAssignAddressRequest
import co.nilin.opex.api.core.inout.ReservedAddressFilter
import co.nilin.opex.api.core.inout.ReservedAddressPage
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.ports.opex.service.OwnerNameResolver
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/admin/address")
@Tag(name = "Address Admin", description = "Admin reports and management of blockchain address assignments.")
class AddressAdminController(
    private val blockchainGatewayProxy: BlockchainGatewayProxy,
    private val ownerNameResolver: OwnerNameResolver
) {

    @PostMapping("/assignments/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search address assignments",
        description = """POST /opex/v1/admin/address/assignments/search.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Every assignment is a separate record, so filtering by `uuid` returns the address history of a user and filtering by `address` returns the holder history of that address. Ordered by assignment date, newest first.
Filters: `uuid`, `address`, `memo`, `status`, `addressTypeId`, `chain`, `assignedFrom` and `assignedTo` (epoch millis), plus `limit` and `offset`. Every filter is optional.
Allowed values:
- status: Assigned, Revoked, Reserved.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AddressAssignmentPage::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun search(
        @RequestBody filter: AddressAssignmentFilter,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressAssignmentPage {
        val page = blockchainGatewayProxy.searchAddressAssignments(
            securityContext.jwtAuthentication().tokenValue(),
            filter
        )
        return page.copy(items = page.items.withNames())
    }

    @GetMapping("/assignments/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get address assignment",
        description = """GET /opex/v1/admin/address/assignments/{id}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns a single assignment record with the holder name.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AddressAssignmentRecord::class))]
            ),
            ApiResponse(responseCode = "404", description = "No assignment with this id.", content = [Content()])
        ]
    )
    suspend fun getAssignment(
        @Parameter(name = "id", description = "Assignment id.", required = true)
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressAssignmentRecord {
        return blockchainGatewayProxy
            .getAddressAssignment(securityContext.jwtAuthentication().tokenValue(), id)
            .withName()
    }

    @PostMapping("/reserved/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List available addresses",
        description = """POST /opex/v1/admin/address/reserved/search.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Returns the addresses that are free right now, which are the only ones `assign` accepts. Call this first, then pass one of the returned `address` plus `memo` pairs to `assign`.
Filters: `addressTypeId`, `chain`, `address`, plus `limit` and `offset`. Every filter is optional.
Note: The list is a snapshot. An address can be taken by another request before it is assigned, in which case `assign` answers 400.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ReservedAddressPage::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden. Required authority is missing: ROLE_admin. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun listAvailable(
        @RequestBody filter: ReservedAddressFilter,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): ReservedAddressPage {
        return blockchainGatewayProxy.searchReservedAddresses(
            securityContext.jwtAuthentication().tokenValue(),
            filter
        )
    }

    @PostMapping("/assign", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Assign an address to a user",
        description = """POST /opex/v1/admin/address/assign.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `uuid` is required. Everything else is optional.
Behavior: Assigns an address from the reserved pool to the user. Either leave `address` out and let the pool pick one, or pass an `address` and `memo` taken from `/reserved/search`. An address outside the pool is never accepted. `expTime` is epoch millis and falls back to the configured address life time.
Address type and chain are not request fields: they belong to the address, so they are taken from the claimed pool entry. To assign on a specific chain, filter `/reserved/search` by `chain` and pass one of its addresses.
Concurrency: The address is taken out of the pool in the same statement that reads it, so two admins cannot assign the same address. Whoever loses the race gets 400 or 409, never a shared address.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AddressAssignmentRecord::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "uuid is missing, the requested address is not in the reserved pool or was just taken, or no address is available.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "409",
                description = "The address is already assigned to someone else.",
                content = [Content()]
            )
        ]
    )
    suspend fun assign(
        @RequestBody request: AdminAssignAddressRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressAssignmentRecord {
        return blockchainGatewayProxy
            .assignAddressByAdmin(securityContext.jwtAuthentication().tokenValue(), request)
            .withName()
    }

    @PostMapping("/assignments/{id}/revoke", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Revoke an address assignment",
        description = """POST /opex/v1/admin/address/assignments/{id}/revoke.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Closes an active assignment. The record stays in the history with its revoke date. Pick the `id` from the search results of the user.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AddressAssignmentRecord::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "The assignment is not active.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "No assignment with this id.", content = [Content()])
        ]
    )
    suspend fun revoke(
        @Parameter(name = "id", description = "Assignment id.", required = true)
        @PathVariable("id") id: Long,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): AddressAssignmentRecord {
        return blockchainGatewayProxy
            .revokeAddressAssignment(securityContext.jwtAuthentication().tokenValue(), id)
            .withName()
    }

    private suspend fun List<AddressAssignmentRecord>.withNames(): List<AddressAssignmentRecord> {
        if (isEmpty()) return this
        val names = ownerNameResolver.resolve(map { it.uuid }.toSet())
        return map { it.copy(fullName = names[it.uuid]) }
    }

    private suspend fun AddressAssignmentRecord.withName(): AddressAssignmentRecord {
        return copy(fullName = ownerNameResolver.resolve(setOf(uuid))[uuid])
    }
}
