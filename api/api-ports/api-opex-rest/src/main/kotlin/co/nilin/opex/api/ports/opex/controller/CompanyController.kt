package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CompanyResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/opex/v1/company")
@Tag(name = "Company", description = "Company the authenticated user represents.")
class CompanyController(val profileProxy: ProfileProxy) {

    @GetMapping
    @Operation(
        summary = "Get company",
        description = """GET /opex/v1/company.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Returns the company the caller is a representative of. Only legal-person profiles have one.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CompanyResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            ),
            ApiResponse(responseCode = "404", description = "Caller does not represent a company.", content = [Content()])
        ]
    )
    suspend fun getCompany(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): CompanyResponse {
        return profileProxy.getCompany(securityContext.jwtAuthentication().tokenValue())
    }
}
