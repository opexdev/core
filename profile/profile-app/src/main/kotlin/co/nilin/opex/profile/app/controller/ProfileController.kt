package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.otp.TempOtpResponse
import co.nilin.opex.profile.core.data.profile.CompleteProfileRequest
import co.nilin.opex.profile.core.data.profile.CompleteProfileResponse
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.UpdateProfileRequest
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


    @PutMapping("")
    suspend fun update(
        @RequestBody newProfile: UpdateProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): Profile? {
        return profileManagement.update(securityContext.authentication.name, newProfile)?.awaitFirstOrNull()
    }

    @PostMapping("/Completion")
    suspend fun completeProfile(
        @RequestBody completeProfileRequest: CompleteProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): CompleteProfileResponse? {
        return profileManagement.completeProfile(securityContext.authentication.name, completeProfileRequest)
    }

    //TODO use dto for input
    @PutMapping("/request/mobile/{mobile}")
    suspend fun requestUpdateMobile(
        @PathVariable mobile: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        return profileManagement.requestUpdateMobile(securityContext.authentication.name, mobile)
    }

    @PutMapping("/mobile/{mobile}/{otpCode}")
    suspend fun updateMobile(
        @PathVariable mobile: String,
        @PathVariable otpCode: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileManagement.updateMobile(securityContext.authentication.name, mobile, otpCode)
    }

    //TODO use dto for input
    @PutMapping("/request/email/{email}")
    suspend fun requestUpdateEmail(
        @PathVariable email: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        return profileManagement.requestUpdateEmail(securityContext.authentication.name, email)
    }

    @PutMapping("/email/{email}/{otpCode}")
    suspend fun updateEmail(
        @PathVariable email: String,
        @PathVariable otpCode: String,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileManagement.updateEmail(securityContext.authentication.name, email, otpCode)
    }
}