package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.toProfileApprovalRequestUserResponse
import co.nilin.opex.api.ports.opex.util.toProfileResponse
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/profile")

class ProfileController(
    val profileProxy: ProfileProxy,
) {

    @GetMapping("/personal-data")
    suspend fun getProfile(@CurrentSecurityContext securityContext: SecurityContext): ProfileResponse {
        return profileProxy.getProfile(securityContext.jwtAuthentication().tokenValue()).toProfileResponse()
    }

    @PutMapping("/completion")
    suspend fun completeProfile(
        @RequestBody completeProfileRequest: CompleteProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): ProfileResponse? {
        return profileProxy.completeProfile(securityContext.jwtAuthentication().tokenValue(), completeProfileRequest)
            ?.toProfileResponse()
    }

    @PostMapping("/contact/update/otp-request")
    suspend fun requestContactUpdate(
        @RequestBody request: ContactUpdateRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        return profileProxy.requestContactUpdate(securityContext.jwtAuthentication().tokenValue(), request)
    }

    @PatchMapping("/contact/update/otp-verification")
    suspend fun confirmContactUpdate(
        @RequestBody request: ContactUpdateConfirmRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileProxy.confirmContactUpdate(securityContext.jwtAuthentication().tokenValue(), request)

    }

    @GetMapping("/approval-request")
    suspend fun getApprovalRequest(@CurrentSecurityContext securityContext: SecurityContext): ProfileApprovalRequestUserResponse {
        return profileProxy.getUserProfileApprovalRequest(securityContext.jwtAuthentication().tokenValue())
            .toProfileApprovalRequestUserResponse()
    }
}