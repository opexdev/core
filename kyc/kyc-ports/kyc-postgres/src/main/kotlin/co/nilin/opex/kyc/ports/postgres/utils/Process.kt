package co.nilin.opex.kyc.ports.postgres.utils

import co.nilin.opex.kyc.core.data.*
import co.nilin.opex.kyc.ports.postgres.dao.KycProcessRepository
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusRepository
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull


suspend fun KycRequest.verifyRequest(kycProcessRepository: KycProcessRepository, userStatusRepository: UserStatusRepository): UserStatusModel? =

        when (this.step) {
            KycStep.Register -> {
                null
            }

            KycStep.UploadDataForLevel2 -> {
                val request = this as UploadDataRequest
                request.filesPath?.let { if (it.size != 3) throw throw OpexException(OpexError.InvalidUploadFileRequest) }
                        ?: throw OpexException(OpexError.InvalidUploadFileRequest)

                val previousValidSteps = KycLevelDetail.UploadDataLevel2.previousValidSteps

                userStatusRepository.findByUserId(this.userId)?.awaitFirstOrNull()?.let {
                    if (previousValidSteps != null && it.kycLevel !in previousValidSteps) {
                        throw OpexException(OpexError.InvalidKycRequest)
                    }
                    it

                } ?: run { throw OpexException(OpexError.UserNotFound) }
            }

            KycStep.ManualReview -> {
                if ((this as ManualReviewRequest).status !in arrayListOf(KycStatus.Accepted, KycStatus.Rejected))
                    throw OpexException(OpexError.InvalidStatus)

                val previousValidSteps = if (this.status == KycStatus.Accepted) KycLevelDetail.AcceptedManualReview.previousValidSteps
                else KycLevelDetail.RejectedManualReview.previousValidSteps

                kycProcessRepository.findByUserIdAndStepId(this.userId, this.referenceId!!)?.awaitFirstOrNull()?.let {
                    userStatusRepository.findByUserId(this.userId)?.awaitFirstOrNull()?.let {
                        if ((previousValidSteps != null) && it.kycLevel !in previousValidSteps) {
                            throw OpexException(OpexError.InvalidKycRequest)
                        }
                        it
                    } ?: throw OpexException(OpexError.UserNotFound)

                } ?: run { throw OpexException(OpexError.BadReviewRequest) }
            }

            KycStep.ManualUpdate -> {
                if ((this as ManualUpdateRequest).level !in arrayListOf(KycLevelDetail.ManualUpdateLevel1, KycLevelDetail.ManualUpdateLevel2))
                    throw OpexException(OpexError.InvalidKycUpdateRequest)
                userStatusRepository.findByUserId(this.userId)?.awaitFirstOrNull()
            }

            else -> {
                null
            }
        }


// fun KycDataRequest.verify() {
//    step?.let {
//        if (step !in arrayListOf(KycLevelDetail.UploadDataLevel2))
//            throw OpexException(OpexError.InvalidRequestBody)
//    }
//    status?.let {
//        if (status !in arrayListOf(KycStatus.Accepted, KycStatus.Rejected))
//            throw OpexException(OpexError.InvalidRequestBody)
//    }
//}
