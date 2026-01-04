package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.CompleteProfileRequest
import co.nilin.opex.api.core.ContactUpdateConfirmRequest
import co.nilin.opex.api.core.ContactUpdateRequest
import co.nilin.opex.api.core.ProfileApprovalUserResponse
import co.nilin.opex.api.core.inout.Profile
import co.nilin.opex.api.core.inout.TempOtpResponse
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
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
    suspend fun getProfile(@CurrentSecurityContext securityContext: SecurityContext): Profile {
        return profileProxy.getProfile(securityContext.jwtAuthentication().tokenValue())
    }

    @PutMapping("/completion")
    suspend fun completeProfile(
        @RequestBody completeProfileRequest: CompleteProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Profile? {
        return profileProxy.completeProfile(securityContext.jwtAuthentication().tokenValue(), completeProfileRequest)
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
    suspend fun getApprovalRequest(@CurrentSecurityContext securityContext: SecurityContext): ProfileApprovalUserResponse {
        return profileProxy.getUserProfileApprovalRequest(securityContext.jwtAuthentication().tokenValue())
    }
}