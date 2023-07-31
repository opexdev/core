package co.nilin.opex.kyc.ports.postgres.imp

import co.nilin.opex.core.data.*
import org.springframework.stereotype.Component
import co.nilin.opex.core.spi.KYCPersister
import co.nilin.opex.kyc.ports.postgres.dao.KycProcessRepository
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusRepository
import co.nilin.opex.kyc.ports.postgres.model.base.UserStatus
import co.nilin.opex.kyc.ports.postgres.model.entity.KycProcessModel
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import co.nilin.opex.kyc.ports.postgres.utils.verifyRequest
import co.nilin.opex.profile.core.data.profile.KycLevel
import co.nilin.opex.profile.core.data.profile.KycLevelDetail
import co.nilin.opex.profile.core.utils.convert
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional
class KycManagementImp(private val kycProcessRepository: KycProcessRepository,
                       private val userStatusRepository: UserStatusRepository) : KYCPersister {
    override suspend fun kycProcess(kycRequest: KycRequest): KycResponse ?{
        kycRequest.verifyRequest()
        return when (kycRequest.step) {
            KycStep.Register -> registerNewUser(kycRequest)
            KycStep.UploadDataForLevel2 -> uploadData(kycRequest as UploadDataRequest)
            KycStep.ManualReview -> reviewManually(kycRequest as ManualReviewRequest)
            KycStep.ManualUpdate -> updateManually(kycRequest as ManualUpdateRequest)

            else -> {
                null
            }
        }
    }

    suspend fun registerNewUser(kycRequest: KycRequest): KycResponse {

        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = KycStatus.Successful
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = KycLevelDetail.Registered
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }

    suspend fun uploadData(kycRequest: UploadDataRequest): KycResponse {


        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = KycStatus.Successful
        kycProcessModel.input = kycRequest.filesPath.keys.joinToString("#")
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = KycLevelDetail.UploadDataForLevel2
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }

    suspend fun reviewManually(kycRequest: ManualReviewRequest): KycResponse {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = kycRequest.status
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.status == KycStatus.Accept) KycLevelDetail.AcceptedManualReview else KycLevelDetail.RejectedManualReview
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }


    suspend fun updateManually(kycRequest: ManualUpdateRequest):KycResponse {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.kycLevel == KycLevel.Level1) KycLevelDetail.Registered
                    else if (kycRequest.kycLevel == KycLevel.Level2) KycLevelDetail.AcceptedManualReview
                    else throw OpexException(OpexError.Error)
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = "system_${kycRequest.processId}"
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }


}