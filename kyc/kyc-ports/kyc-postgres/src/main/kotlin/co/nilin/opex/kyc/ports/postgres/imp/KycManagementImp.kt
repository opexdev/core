package co.nilin.opex.kyc.ports.postgres.imp

import co.nilin.opex.kyc.core.data.*
import org.springframework.stereotype.Component
import co.nilin.opex.kyc.core.spi.KYCPersister
import co.nilin.opex.kyc.ports.postgres.dao.KycProcessRepository
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusRepository
import co.nilin.opex.kyc.ports.postgres.model.base.UserStatus
import co.nilin.opex.kyc.ports.postgres.model.entity.KycProcessModel
import co.nilin.opex.kyc.ports.postgres.model.entity.UserStatusModel
import co.nilin.opex.kyc.ports.postgres.utils.verifyRequest
import co.nilin.opex.kyc.core.data.KycLevel
import co.nilin.opex.kyc.core.data.KycLevelDetail
import co.nilin.opex.kyc.core.utils.convert
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional
class KycManagementImp(private val kycProcessRepository: KycProcessRepository,
                       private val userStatusRepository: UserStatusRepository) : KYCPersister {
    private val logger = LoggerFactory.getLogger(KycManagementImp::class.java)

    override suspend fun kycProcess(kycRequest: KycRequest): KycResponse? {
        val previousUserStatus= kycRequest.verifyRequest(kycProcessRepository, userStatusRepository)

        return when (kycRequest.step) {
            KycStep.Register -> registerNewUser(kycRequest)
            KycStep.UploadDataForLevel2 -> uploadData(kycRequest as UploadDataRequest,previousUserStatus)
            KycStep.ManualReview -> reviewManually(kycRequest as ManualReviewRequest,previousUserStatus)
            KycStep.ManualUpdate -> updateManually(kycRequest as ManualUpdateRequest,previousUserStatus)

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

    suspend fun uploadData(kycRequest: UploadDataRequest,previousUserStatus:Long?): KycResponse {


        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = KycStatus.Successful
        kycProcessModel.input = kycRequest.filesPath!!.values.joinToString("#")
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = KycLevelDetail.UploadDataLevel2
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java).apply { previousUserStatus?.let { id=previousUserStatus } })
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }

    suspend fun reviewManually(kycRequest: ManualReviewRequest,previousUserStatus:Long?): KycResponse {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = kycRequest.status
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.status == KycStatus.Accept) KycLevelDetail.AcceptedManualReview else KycLevelDetail.RejectedManualReview
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = kycRequest.processId
                }.convert(UserStatusModel::class.java).apply { previousUserStatus?.let { id=previousUserStatus } })
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }


    suspend fun updateManually(kycRequest: ManualUpdateRequest,previousUserStatus:Long?): KycResponse {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status=KycStatus.Successful
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.kycLevel == KycLevel.Level1) KycLevelDetail.Registered
                    else if (kycRequest.kycLevel == KycLevel.Level2) KycLevelDetail.AcceptedManualReview
                    else throw OpexException(OpexError.Error)
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    processId = "system_${kycRequest.processId}"
                    detail=kycRequest.step?.name
                }.convert(UserStatusModel::class.java).apply { previousUserStatus?.let { id=previousUserStatus } })
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.processId!!)
    }


    suspend fun getKycData(kycDataRequest: KycDataRequest){


        kycProcessRepository.


    }

}