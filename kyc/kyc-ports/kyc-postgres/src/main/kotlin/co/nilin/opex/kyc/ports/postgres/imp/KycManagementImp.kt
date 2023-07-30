package co.nilin.opex.kyc.ports.postgres.imp

import co.nilin.opex.core.data.KycRequest
import co.nilin.opex.core.data.KycStatus
import co.nilin.opex.core.data.KycStep
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
    override suspend fun kycProcess(kycRequest: KycRequest) {
        kycRequest.verifyRequest()
        when (kycRequest.step) {
            KycStep.Register -> registerNewUser(kycRequest)
            KycStep.UploadDataForLevel2 -> uploadData(kycRequest)
            KycStep.ManualReview -> reviewManually(kycRequest)
            KycStep.ManualUpdate -> updateManually(kycRequest)

        }
    }

    suspend fun registerNewUser(kycRequest: KycRequest) {

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

    }

    suspend fun uploadData(kycRequest: KycRequest) {


        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = KycStatus.Successful
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = KycLevelDetail.UploadDataForLevel2
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
return kycRequest

    }

    suspend fun reviewManually(kycRequest: KycRequest) {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = kycRequest.status
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.status == KycStatus.Accept) KycLevelDetail.SuccessfulManualReview else KycLevelDetail.FailedManualReview
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
    }


    suspend fun updateManually(kycRequest: KycRequest) {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = kycRequest.status
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.kycLevel == KycLevel.Level1) KycLevelDetail.Registered else KycLevelDetail.SuccessfulManualReview
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = "system"
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
    }


}