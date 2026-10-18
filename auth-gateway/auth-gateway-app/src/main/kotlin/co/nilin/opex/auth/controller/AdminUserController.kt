package co.nilin.opex.auth.controller

import co.nilin.opex.auth.model.CreateLegalRepresentativeRequest
import co.nilin.opex.auth.model.CreateLegalRepresentativeResponse
import co.nilin.opex.auth.model.SendCredentialsResponse
import co.nilin.opex.auth.service.AdminUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/user")
@Tag(name = "User Admin", description = "Admin user provisioning operations.")
class AdminUserController(private val adminUserService: AdminUserService) {

    @PostMapping("/legal-representative")
    @Operation(
        summary = "Create legal representative",
        description = """POST /v1/admin/user/legal-representative.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Validation: `firstName`, `lastName`, `email` and `mobile` are required.
Behavior: Creates an enabled Keycloak user without a password and publishes the user-created event so the profile and wallet are provisioned. Call the credentials endpoint afterwards to set and deliver a password.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateLegalRepresentativeResponse::class))]
            ),
            ApiResponse(responseCode = "409", description = "A user with this email already exists.", content = [Content()])
        ]
    )
    suspend fun createLegalRepresentative(
        @RequestBody request: CreateLegalRepresentativeRequest
    ): CreateLegalRepresentativeResponse {
        return adminUserService.createLegalRepresentative(request)
    }

    @PostMapping("/{uuid}/credentials")
    @Operation(
        summary = "Set and email credentials",
        description = """POST /v1/admin/user/{uuid}/credentials.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Generates a random 10-character password, stores it in Keycloak and emails it to the user. The password is never returned.
Response: `emailSent` reports whether delivery succeeded. A false value means the password was set but not delivered.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = SendCredentialsResponse::class))]
            )
        ]
    )
    suspend fun sendCredentials(
        @Parameter(name = "uuid", description = "Keycloak user id.", required = true)
        @PathVariable("uuid") uuid: String
    ): SendCredentialsResponse {
        return adminUserService.sendCredentials(uuid)
    }

    @DeleteMapping("/{uuid}")
    @Operation(
        summary = "Delete user",
        description = """DELETE /v1/admin/user/{uuid}.
Security: Bearer admin-token required. Required authority: ROLE_admin.
Behavior: Removes the Keycloak user. Used to compensate a failed provisioning flow.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [ApiResponse(responseCode = "200", description = "User deleted.")]
    )
    suspend fun deleteUser(
        @Parameter(name = "uuid", description = "Keycloak user id.", required = true)
        @PathVariable("uuid") uuid: String
    ) {
        adminUserService.deleteUser(uuid)
    }
}
