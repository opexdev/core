package co.nilin.opex.kyc.app.controller

import co.nilin.opex.kyc.core.data.*
import co.nilin.opex.kyc.app.service.KycManagement
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.core.data.ManualReviewRequest
import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import co.nilin.opex.kyc.core.data.UploadDataRequest
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/v2/kyc")
class KycController(private val kycManagement: KycManagement) {

    @PostMapping("/upload/{userId}")
    suspend fun uploadData(@PathVariable("userId") userId: String,
                           @RequestBody uploadDataRequest: UploadDataRequest,
                           @RequestParam("frame1") frame1: FilePart,
                           @RequestParam("frame2") frame2: FilePart,
                           @RequestParam("frame3") frame3: FilePart
    ) {
        //todo check token
        uploadDataRequest.userId = userId
        uploadDataRequest.step = KycStep.UploadDataForLevel2
        uploadDataRequest.processId = UUID.randomUUID().toString()
        var fails = mutableMapOf<String, FilePart>()
        fails[frame1.filename()]=frame1
        fails[frame2.filename()]=frame2
        fails[frame3.filename()]=frame3
        uploadDataRequest.files=fails
        kycManagement.uploadData(uploadDataRequest)

    }

    //todo just admin
    @PostMapping("/review/{processId}")
    suspend fun manualReview(@PathVariable("processId") processId: String,
                             @RequestBody  manualReviewRequest: ManualReviewRequest,

                             ) {
        manualReviewRequest.processId = processId
        manualReviewRequest.step = KycStep.ManualReview
        kycManagement.manualReview(manualReviewRequest)
    }
    //todo just admin

    @PutMapping("/{userId}")
    suspend fun manualUpdate(@PathVariable("userId") userId: String,
                             @RequestBody  manualUpdateRequest: ManualUpdateRequest,
                             ) {
        manualUpdateRequest.userId = userId
        manualUpdateRequest.processId = UUID.randomUUID().toString()
        manualUpdateRequest.step = KycStep.ManualUpdate
        kycManagement.manualUpdate(manualUpdateRequest)
    }


}