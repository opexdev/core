package co.nilin.opex.kyc.app.controller

import co.nilin.opex.kyc.app.service.KycManagement
import co.nilin.opex.core.data.KycRequest
import co.nilin.opex.core.data.KycStep
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/v2/kyc")
class KycController(private val kycManagement: KycManagement) {

    @PostMapping("/{userId}")
    suspend fun updateKYcLevel(@PathVariable("userId") userId: String,
                               @RequestBody kycRequest: KycRequest,
                               @RequestParam("frame1") frame1:MultipartFile,
                               @RequestParam("frame2") frame2:MultipartFile,
                               @RequestParam("frame3") frame3:MultipartFile
    ) {
        //todo check token
        kycRequest.userId = userId
        kycRequest.step=KycStep.UploadDataForLevel2
        kycRequest.processId=UUID.randomUUID().toString()
        kycRequest.frame1=frame1
        kycRequest.frame2=frame2
        kycRequest.frame3=frame3
        kycManagement.kycProcess(kycRequest)

    }

}