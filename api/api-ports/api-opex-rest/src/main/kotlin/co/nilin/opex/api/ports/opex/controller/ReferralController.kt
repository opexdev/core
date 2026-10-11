package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CreateReferralCodeRequest
import co.nilin.opex.api.core.inout.ReferralCodeDto
import co.nilin.opex.api.core.spi.ReferralProxy
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
@RequestMapping("/opex/v1/referral")
@Tag(name = "Referral", description = "Referral code operations.")
class ReferralController(private val referralProxy: ReferralProxy) {

    @GetMapping("/codes/commission-share-steps", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get commission share steps",
        description = """GET /opex/v1/referral/codes/commission-share-steps.
Security: Public endpoint. No Bearer token is required.
Behavior: Returns the percentages the client may offer for the mutual referrer/referent commission share, e.g. [0, 5, 10, 15, 20, 25, 30].""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = Int::class))
                )]
            )
        ]
    )
    suspend fun getCommissionShareSteps(): List<Int> {
        return referralProxy.getCommissionShareSteps()
    }

    @PostMapping("/codes", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Create referral code",
        description = """POST /opex/v1/referral/codes.
Security: Bearer user-token required. Requires authenticated user JWT.
Validation: `commissionShare` must be one of the values returned by GET /opex/v1/referral/codes/commission-share-steps.
Behavior: Creates a referral code for the caller and returns it. `commissionShare` is the percentage of the trade fee shared with both the referrer and the referent. A user may hold at most one code per distinct `commissionShare`.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response. The generated referral code.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun generateReferralCode(
        @RequestBody request: CreateReferralCodeRequest,
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): String {
        return referralProxy.generateReferralCode(
            securityContext.jwtAuthentication().tokenValue(),
            request.commissionShare
        )
    }

    @GetMapping("/codes/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get referral code",
        description = """GET /opex/v1/referral/codes/{code}.
Security: Public endpoint. No Bearer token is required.
Behavior: Returns the referral code detail along with the number of users referred by it.""",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ReferralCodeDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Referral code not found.", content = [Content()])
        ]
    )
    suspend fun getReferralCode(
        @Parameter(name = "code", description = "Referral code.", required = true)
        @PathVariable code: String
    ): ReferralCodeDto {
        return referralProxy.getReferralCode(code)
    }

    @GetMapping("/codes", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List referral codes",
        description = """GET /opex/v1/referral/codes.
Security: Bearer user-token required. Requires authenticated user JWT.
Behavior: Returns all referral codes owned by the caller, each with the number of users referred by it.""",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = ReferralCodeDto::class))
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized. Bearer token is missing, invalid, or expired. No response body.",
                content = [Content()]
            )
        ]
    )
    suspend fun getReferralCodes(
        @Parameter(hidden = true)
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<ReferralCodeDto> {
        return referralProxy.getReferralCodes(securityContext.jwtAuthentication().tokenValue())
    }
}
