package co.nilin.opex.kyc.app.controller

import co.nilin.opex.kyc.app.service.KycManagement
import data.UpdateKYCLevelRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/kyc")
class KycController(private val kycManagement: KycManagement) {

    @PostMapping("/{userId}")
    suspend fun updateKYcLevel(@PathVariable("userId") userId: String,
                               @RequestBody updateKYCLevelRequest: UpdateKYCLevelRequest) {
        updateKYCLevelRequest.userId = userId
        kycManagement.updateKycLevel(updateKYCLevelRequest)

    }

}