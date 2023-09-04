package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import co.nilin.opex.profile.core.data.profile.UpdateProfileRequest
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v2/profile")

class ProfileController(val profileManagement: ProfileManagement) {

    @GetMapping("")
    suspend fun getProfile(@CurrentSecurityContext securityContext: SecurityContext): Profile? {
        return profileManagement.getProfile(securityContext.authentication.name)?.awaitFirstOrNull()
    }


    @PutMapping("")
    suspend fun update( @RequestBody newProfile: UpdateProfileRequest,
                       @CurrentSecurityContext securityContext: SecurityContext): Profile? {
        return profileManagement.update(securityContext.authentication.name, newProfile)?.awaitFirstOrNull()
    }


}