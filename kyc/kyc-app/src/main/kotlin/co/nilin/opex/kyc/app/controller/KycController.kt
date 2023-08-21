package co.nilin.opex.kyc.app.controller

import co.nilin.opex.kyc.core.data.*
import co.nilin.opex.kyc.app.service.KycManagement
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.core.data.ManualReviewRequest
import co.nilin.opex.kyc.core.data.ManualUpdateRequest
import co.nilin.opex.kyc.core.data.UploadDataRequest
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusHistoryRepository
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitLast
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/v2/kyc")
class KycController(private val kycManagement: KycManagement) {
    private val logger = LoggerFactory.getLogger(KycController::class.java)

    @PostMapping("/upload/{userId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadData(@PathVariable("userId") userId: String,
                           @RequestPart("files") partFlux: Flux<FilePart>,
                           @CurrentSecurityContext securityContext: SecurityContext
    ) {
        if (userId != securityContext.authentication.name)
            throw OpexException(OpexError.Forbidden)
        partFlux.switchIfEmpty { throw OpexException(OpexError.InvalidRequestBody) }
        var files = mutableMapOf<String, FilePart>()
        partFlux.log().doOnNext { f -> files[f.filename()] = f }.awaitLast()
        if (files.isEmpty() || files == null)
            throw OpexException(OpexError.InvalidRequestBody)
        var uploadDataRequest = UploadDataRequest(files = files).apply {
            this.userId = userId
            step = KycStep.UploadDataForLevel2
            stepId = UUID.randomUUID().toString()
        }
        kycManagement.uploadData(uploadDataRequest)
    }


    @GetMapping("/step/{stepId}")
    suspend fun getData(
            @PathVariable("stepId") stepId: String, @CurrentSecurityContext securityContext: SecurityContext
    ): KycProcessDetail? {
        return kycManagement.getStepData(stepId, securityContext.authentication.name)
    }


}