package co.nilin.opex.kyc.ports.postgres.utils

import co.nilin.opex.kyc.core.data.KycRequest
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.ports.postgres.dao.KycProcessRepository
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusRepository
import co.nilin.opex.profile.core.data.profile.KycLevelDetail
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException

class Process

lateinit var kycProcessRepository: KycProcessRepository
lateinit var userStatusRepository: UserStatusRepository
fun KycRequest.verifyRequest() {

    when (this.step) {
        KycStep.Register -> {
        }

        KycStep.UploadDataForLevel2 -> {
            val previousValidSteps = KycLevelDetail.UploadDataForLevel2.previousValidSteps
            userStatusRepository.findByUserId(this.userId)?.let {
                if (previousValidSteps != null) {
                    if (it.kycLevel in previousValidSteps) {
                    } else
                        throw OpexException(OpexError.Error)
                }
            }
                    ?: run { OpexException(OpexError.UserNotFound) }
        }

        KycStep.ManualReview -> {
            val previousValidSteps = KycLevelDetail.AcceptedManualReview.previousValidSteps
            userStatusRepository.findByUserIdAndProcessId(this.userId, this.processId!!)?.let {
                if (previousValidSteps != null) {
                    if (it.kycLevel in previousValidSteps) {
                    } else
                        throw OpexException(OpexError.Error)
                }
            }
                    ?: run { OpexException(OpexError.UserNotFound) }
        }

        KycStep.ManualUpdate -> {
        }
    }
}