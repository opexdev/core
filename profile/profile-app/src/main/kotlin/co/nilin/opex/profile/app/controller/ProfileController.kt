package co.nilin.opex.profile.app.controller

import co.nilin.opex.common.OpexError
import co.nilin.opex.profile.app.dto.ContactUpdateConfirmRequest
import co.nilin.opex.profile.app.dto.ContactUpdateRequest
import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.otp.TempOtpResponse
import co.nilin.opex.profile.core.data.profile.CompleteProfileRequest
import co.nilin.opex.profile.core.data.profile.CompleteProfileResponse
import co.nilin.opex.profile.core.data.profile.Profile
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping

class ProfileController(val profileManagement: ProfileManagement) {

    @GetMapping("")
    suspend fun getProfile(@CurrentSecurityContext securityContext: SecurityContext): Profile? {
        return profileManagement.getProfile(securityContext.authentication.name)?.awaitFirstOrNull()
    }

//    @PutMapping("")
//    suspend fun update(
//        @RequestBody newProfile: UpdateProfileRequest,
//        @CurrentSecurityContext securityContext: SecurityContext
//    ): Profile? {
//        return profileManagement.update(securityContext.authentication.name, newProfile)?.awaitFirstOrNull()
//    }

    @PutMapping("/completion")
    suspend fun completeProfile(
        @RequestBody completeProfileRequest: CompleteProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): CompleteProfileResponse? {
        return profileManagement.completeProfile(securityContext.authentication.name, completeProfileRequest)
    }

    @PostMapping("/contact/update/otp-request")
    suspend fun requestContactUpdate(
        @RequestBody request: ContactUpdateRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        val username = securityContext.authentication.name
        return if (!request.email.isNullOrBlank()) {
            profileManagement.requestUpdateEmail(username, request.email)
        } else if (!request.mobile.isNullOrBlank()) {
            profileManagement.requestUpdateMobile(username, request.mobile)
        } else {
            throw OpexError.BadRequest.exception("Either email or mobile must be provided.")
        }
    }

    @PatchMapping("/contact/update/otp-verification")
    suspend fun confirmContactUpdate(
        @RequestBody request: ContactUpdateConfirmRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        val username = securityContext.authentication.name
        if (!request.email.isNullOrBlank()) {
            profileManagement.updateEmail(username, request.email, request.otpCode)
        } else if (!request.mobile.isNullOrBlank()) {
            profileManagement.updateMobile(username, request.mobile, request.otpCode)
        } else {
            throw OpexError.BadRequest.exception("Either email or mobile must be provided.")
        }
    }
}