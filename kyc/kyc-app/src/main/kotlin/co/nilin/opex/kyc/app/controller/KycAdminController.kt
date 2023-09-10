package co.nilin.opex.kyc.app.controller

import co.nilin.opex.kyc.app.service.KycManagement
import co.nilin.opex.kyc.core.data.*
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v2/admin/kyc")
class KycAdminController(private val kycManagement: KycManagement) {

    @PostMapping("/review/{referenceId}")
    suspend fun manualReview(
            @PathVariable("referenceId") referenceId: String,
            @RequestBody manualReviewRequest: ManualReviewRequest,

            ) {
        manualReviewRequest.referenceId = referenceId
        manualReviewRequest.stepId = UUID.randomUUID().toString()
        manualReviewRequest.step = KycStep.ManualReview
        kycManagement.manualReview(manualReviewRequest)
    }

    @PutMapping("/{userId}")
    suspend fun manualUpdate(
            @PathVariable("userId") userId: String,
            @RequestBody manualUpdateRequest: ManualUpdateRequest,
    ) {
        manualUpdateRequest.userId = userId
        manualUpdateRequest.stepId = UUID.randomUUID().toString()
        manualUpdateRequest.step = KycStep.ManualUpdate
        kycManagement.manualUpdate(manualUpdateRequest)
    }


    @GetMapping("/step")
    suspend fun getData(
            @RequestParam("userId") userId: String?,
            @RequestParam("step") step: KycStep?,
            @RequestParam("status") status: KycStatus?,
            @RequestParam("offset") offset: Int?,
            @RequestParam("size") size: Int?
    ): Flow<KycProcess>? {

        return kycManagement.getKycStep(KycDataRequest(userId, step, status, offset ?: 0, size ?: 1000))
    }

    @GetMapping("/step/{stepId}")
    suspend fun getData(
            @PathVariable("stepId") stepId: String
    ): KycProcessDetail? {
        return kycManagement.getStepData(stepId, null)
    }


    @GetMapping("/history/{userId}")
    suspend fun getKycLevelHistory(
            @PathVariable("userId") userId: String
    ): Flow<UserLevelHistory>? {
        return kycManagement.getUserLevelHistory(userId)
    }

    @PutMapping("/internal/{userId}")
    suspend fun internalManualUpdate(
            @PathVariable("userId") userId: String,
            @RequestBody manualUpdateRequest: ManualUpdateRequest,
    ): HttpStatus {
        manualUpdateRequest.userId = userId
        manualUpdateRequest.stepId = UUID.randomUUID().toString()
        manualUpdateRequest.step = KycStep.ManualUpdate
        return kycManagement.manualUpdate(manualUpdateRequest)?.processId?.let { HttpStatus.ACCEPTED }
                ?: HttpStatus.INTERNAL_SERVER_ERROR
    }

}