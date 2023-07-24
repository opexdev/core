package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(ProfileController::class.java)
    @GetMapping("")
    suspend fun getProfiles(@RequestParam offset: Int?, @RequestParam size: Int?): List<Profile>? {
        return profileManagement.getAllProfiles(offset=0, size=1000)
    }

    @GetMapping("/{userId}")
    suspend fun getProfile(@PathVariable("userId") userId: String): Profile? {
       return profileManagement.getProfile(userId)
    }


    @PutMapping("/{userId}")
    suspend fun update(@PathVariable("userId") userId: String,@RequestBody  newProfile: Profile): Profile? {
        return profileManagement.update(userId,newProfile)
    }

    @PostMapping("/{userId}")
    suspend fun createManually(@PathVariable("userId") userId: String,@RequestBody  newProfile: Profile): Profile? {
        return profileManagement.create(userId,newProfile)
    }

    @PutMapping("/admin/{userId}")
    suspend fun updateAsAdmin(@PathVariable("userId") userId: String,@RequestBody  newProfile: Profile): Profile? {
        return profileManagement.updateAsAdmin(userId,newProfile)
    }

    @GetMapping("/history/{userId}")
    suspend fun getHistory(@PathVariable("userId") userId: String,
                           @RequestParam offset: Int?, @RequestParam size: Int?): List<ProfileHistory>? {
        return profileManagement.getHistory(userId,offset=0,size=1000)
    }



}