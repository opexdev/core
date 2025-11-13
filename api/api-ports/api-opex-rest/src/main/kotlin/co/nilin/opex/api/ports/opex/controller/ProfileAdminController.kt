package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
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
        @RequestParam offset: Int?, @RequestParam size: Int?,
        @RequestBody profileRequest: ProfileRequest,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): List<Profile> {
        return profileProxy.getProfiles(
            securityContext.jwtAuthentication().tokenValue(),
            profileRequest,
            size ?: 10,
            offset ?: 0
        )
    }

    @GetMapping("{uuid}")
    suspend fun getProfile(
        @PathVariable uuid: String,
        @CurrentSecurityContext securityContext: SecurityContext,
    ): Profile {
        return profileProxy.getProfile(securityContext.jwtAuthentication().tokenValue(), uuid)
    }
}