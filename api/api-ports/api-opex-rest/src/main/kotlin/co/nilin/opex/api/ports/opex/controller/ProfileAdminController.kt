package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.Profile
import co.nilin.opex.api.core.inout.ProfileHistory
import co.nilin.opex.api.core.inout.ProfileRequest
import co.nilin.opex.api.core.spi.ProfileProxy
import co.nilin.opex.api.ports.opex.util.jwtAuthentication
import co.nilin.opex.api.ports.opex.util.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/opex/v1/admin/profile")
class ProfileAdminController(private val profileProxy: ProfileProxy) {

    @PostMapping
    suspend fun getProfiles(
        @RequestBody profileRequest: ProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Profile> {
        return profileProxy.getProfiles(
            securityContext.jwtAuthentication().tokenValue(),
            profileRequest
        )
    }

    @GetMapping("/{uuid}")
    suspend fun getProfile(
        @PathVariable uuid: String,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Profile {
        return profileProxy.getProfile(securityContext.jwtAuthentication().tokenValue(), uuid)
    }

    @GetMapping("/history/{uuid}")
    suspend fun getProfileHistory(
        @PathVariable uuid: String,
        @RequestParam offset: Int?, @RequestParam limit: Int?,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<ProfileHistory> {
        return profileProxy.getProfileHistory(
            securityContext.jwtAuthentication().tokenValue(),
            uuid,
            limit ?: 10,
            offset ?: 0
        )
    }
}