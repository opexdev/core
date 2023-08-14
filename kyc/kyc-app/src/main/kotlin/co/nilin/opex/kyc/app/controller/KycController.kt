package co.nilin.opex.kyc.app.controller

import co.nilin.opex.kyc.core.data.*
import co.nilin.opex.kyc.app.service.KycManagement
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.core.data.ManualReviewRequest
import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import co.nilin.opex.kyc.core.data.UploadDataRequest
import co.nilin.opex.kyc.ports.postgres.model.base.KycProcess
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import java.awt.PageAttributes
import java.util.*

@RestController
@RequestMapping("/v2/kyc")
class KycController(private val kycManagement: KycManagement) {
    private val logger = LoggerFactory.getLogger(KycController::class.java)

    @PostMapping("/upload/{userId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadData(@PathVariable("userId") userId: String,
                           @RequestPart("files") partFlux: Flux<FilePart>
    ) {
        //todo check token
        var files = mutableMapOf<String, FilePart>()
        partFlux.log().doOnNext { f -> files[f.filename()] = f }.awaitLast()
        if (files.isEmpty() || files == null)
            throw OpexException(OpexError.InvalidRequestBody)
        var uploadDataRequest = UploadDataRequest(files = files).apply {
            this.userId = userId
            step = KycStep.UploadDataForLevel2
            processId = UUID.randomUUID().toString()
        }

        kycManagement.uploadData(uploadDataRequest)

    }

    //todo just admin
    @PostMapping("/review/{processId}")
    suspend fun manualReview(
            @PathVariable("processId") processId: String,
            @RequestBody manualReviewRequest: ManualReviewRequest,

            ) {
        manualReviewRequest.processId = processId
        manualReviewRequest.step = KycStep.ManualReview
        kycManagement.manualReview(manualReviewRequest)
    }
    //todo just admin

    @PutMapping("/{userId}")
    suspend fun manualUpdate(
            @PathVariable("userId") userId: String,
            @RequestBody manualUpdateRequest: ManualUpdateRequest,
    ) {
        manualUpdateRequest.userId = userId
        manualUpdateRequest.processId = UUID.randomUUID().toString()
        manualUpdateRequest.step = KycStep.ManualUpdate
        kycManagement.manualUpdate(manualUpdateRequest)
    }

    @GetMapping("/data/{processId}/{step}/{userId}")
    suspend fun getData(
            @PathVariable("userId") userId: String?,
            @PathVariable("processId") processId: String?,
            @PathVariable("step") step: String?,
    ):Flow<KycProcess> {
        kycManagement.getKycData(KycDataRequest(userId,processId, step))
    }


}