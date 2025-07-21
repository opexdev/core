package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.dto.UpdateEmailRequest
import co.nilin.opex.profile.app.dto.UpdateMobileRequest
import co.nilin.opex.profile.app.dto.VerifyUpdateEmailRequest
import co.nilin.opex.profile.app.dto.VerifyUpdateMobileRequest
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

    @PostMapping("/mobile")
    suspend fun requestUpdateMobile(
        @RequestBody request: UpdateMobileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        return profileManagement.requestUpdateMobile(securityContext.authentication.name, request.mobile)
    }

    @PutMapping("/mobile")
    suspend fun verifyUpdateMobile(
        @RequestBody request: VerifyUpdateMobileRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileManagement.updateMobile(securityContext.authentication.name, request.mobile, request.otp)
    }

    @PostMapping("/email")
    suspend fun requestUpdateEmail(
        @RequestBody request: UpdateEmailRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): TempOtpResponse {
        return profileManagement.requestUpdateEmail(securityContext.authentication.name, request.email)
    }

    @PutMapping("/email")
    suspend fun verifyUpdateEmail(
        @RequestBody request: VerifyUpdateEmailRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ) {
        profileManagement.updateEmail(securityContext.authentication.name, request.email, request.otp)
    }
}