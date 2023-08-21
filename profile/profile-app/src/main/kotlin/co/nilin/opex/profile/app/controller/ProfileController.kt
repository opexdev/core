package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
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
@RequestMapping("/v1/profile")

class ProfileController(val profileManagement: ProfileManagement) {

    @GetMapping("/{userId}")
    suspend fun getProfile(@PathVariable("userId") userId: String): Profile? {
        return profileManagement.getProfile(userId)
    }


    @PutMapping("/{userId}")
    suspend fun update(@PathVariable("userId") userId: String, @RequestBody newProfile: Profile,
                       @CurrentSecurityContext securityContext: SecurityContext): Profile? {
        if (securityContext.authentication.name != userId)
            throw OpexException(OpexError.Forbidden)
        return profileManagement.update(userId, newProfile)
    }


}