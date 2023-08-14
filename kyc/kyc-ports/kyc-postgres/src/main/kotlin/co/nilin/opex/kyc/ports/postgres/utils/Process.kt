package co.nilin.opex.kyc.ports.postgres.utils

import co.nilin.opex.kyc.core.data.KycLevelDetail
import co.nilin.opex.kyc.core.data.KycRequest
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.core.data.UploadDataRequest
import co.nilin.opex.kyc.ports.postgres.dao.KycProcessRepository
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusRepository
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull


suspend fun KycRequest.verifyRequest(kycProcessRepository: KycProcessRepository, userStatusRepository: UserStatusRepository): Long? =

        when (this.step) {
            KycStep.Register -> {
                null
            }

            KycStep.UploadDataForLevel2 -> {
                val request = this as UploadDataRequest
                request.filesPath?.let { if (it.size != 3) throw throw OpexException(OpexError.InvalidRequestBody) }
                        ?: throw OpexException(OpexError.InvalidRequestBody)
                val previousValidSteps = KycLevelDetail.UploadDataLevel2.previousValidSteps

                userStatusRepository.findByUserId(this.userId)?.awaitFirstOrNull()?.let {
                    if (previousValidSteps != null) {
                        if (it.kycLevel in previousValidSteps) {
                            return it.id
                        } else
                            throw OpexException(OpexError.Error)
                    } else
                        null
                }
                        ?: run { throw OpexException(OpexError.UserNotFound) }
            }

            KycStep.ManualReview -> {
                val previousValidSteps = KycLevelDetail.AcceptedManualReview.previousValidSteps
                userStatusRepository.findByUserIdAndProcessId(this.userId, this.processId!!)?.awaitFirstOrNull()?.let {
                    if (previousValidSteps != null) {
                        if (it.kycLevel in previousValidSteps) {
                            userStatusRepository.findByUserId(this.userId)?.awaitFirstOrNull()?.id
                        } else
                            throw OpexException(OpexError.Error)
                    } else
                        null
                }
                        ?: run { throw OpexException(OpexError.UserNotFound) }
            }

            KycStep.ManualUpdate -> {
                userStatusRepository.findByUserId(this.userId)?.awaitFirstOrNull()?.id
            }

            else -> {
                null
            }
        }
