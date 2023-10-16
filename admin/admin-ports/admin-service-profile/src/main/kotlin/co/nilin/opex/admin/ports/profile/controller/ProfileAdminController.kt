package co.nilin.opex.admin.ports.profile.controller

import co.nilin.opex.admin.core.data.ProfileRequest
import co.nilin.opex.admin.core.data.ProfileResponse
import co.nilin.opex.admin.ports.profile.service.ProfileService
import co.nilin.opex.profile.core.data.profile.Profile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.ws.rs.PUT

@RestController
@RequestMapping("/admin/v2/profile")
class ProfileAdminController(private val profileService: ProfileService) {
    private val logger = LoggerFactory.getLogger(ProfileAdminController::class.java)

    @PostMapping("")
    suspend fun getProfile(@RequestBody profileRequest: ProfileRequest): List<ProfileResponse>? {
        return profileService.getProfile(profileRequest)?.toList()
    }



}