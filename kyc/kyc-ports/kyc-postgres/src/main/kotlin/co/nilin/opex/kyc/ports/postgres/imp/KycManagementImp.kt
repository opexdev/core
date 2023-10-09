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
import co.nilin.opex.kyc.ports.postgres.dao.UserStatusHistoryRepository
import co.nilin.opex.kyc.ports.postgres.model.history.UserStatusHistory
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
@Transactional
class KycManagementImp(private val kycProcessRepository: KycProcessRepository,
                       private val userStatusRepository: UserStatusRepository,
                       private val userStatusHistoryRepository: UserStatusHistoryRepository) : KYCPersister {
    private val logger = LoggerFactory.getLogger(KycManagementImp::class.java)

    override suspend fun kycProcess(kycRequest: KycRequest): KycResponse? {
        val previousUserStatus = kycRequest.verifyRequest(kycProcessRepository, userStatusRepository)

        return when (kycRequest.step) {
            KycStep.Register -> registerNewUser(kycRequest)
            KycStep.UploadDataForLevel2 -> uploadData(kycRequest as UploadDataRequest, previousUserStatus)
            KycStep.ManualReview -> reviewManually(kycRequest as ManualReviewRequest, previousUserStatus)
            KycStep.ManualUpdate -> updateManually(kycRequest as ManualUpdateRequest, previousUserStatus)

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
                    referenceId = kycRequest.stepId
                }.convert(UserStatusModel::class.java))
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.stepId!!)
    }

    suspend fun uploadData(kycRequest: UploadDataRequest, previousUserStatus: Long?): KycResponse {


        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = KycStatus.Successful
        kycProcessModel.input = kycRequest.filesPath!!.values.joinToString("#")
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = KycLevelDetail.UploadDataLevel2
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    referenceId = kycRequest.stepId
                }.convert(UserStatusModel::class.java).apply { previousUserStatus?.let { id = previousUserStatus } })
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.stepId!!)
    }

    suspend fun reviewManually(kycRequest: ManualReviewRequest, previousUserStatus: Long?): KycResponse {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = kycRequest.status
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = if (kycRequest.status == KycStatus.Accepted) KycLevelDetail.AcceptedManualReview else KycLevelDetail.RejectedManualReview
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    referenceId = kycRequest.stepId
                }.convert(UserStatusModel::class.java).apply { previousUserStatus?.let { id = previousUserStatus } })
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.stepId!!)
    }

    //todo
    // set "old level" and "new level" in description
    suspend fun updateManually(kycRequest: ManualUpdateRequest, previousUserStatus: Long?): KycResponse {
        var kycProcessModel = kycRequest.convert(KycProcessModel::class.java)
        kycProcessModel.status = KycStatus.Successful
        kycProcessRepository.save(kycProcessModel).zipWith(
                userStatusRepository.save(UserStatus().apply {
                    kycLevel = kycRequest.level
                    lastUpdateDate = LocalDateTime.now()
                    userId = kycRequest.userId
                    referenceId = kycRequest.stepId
                    detail = kycRequest.step?.name
                }.convert(UserStatusModel::class.java).apply { previousUserStatus?.let { id = previousUserStatus } })
        ).awaitFirstOrNull()
        return KycResponse(processId = kycRequest.stepId!!)
    }


    override suspend fun getSteps(kycDataRequest: KycDataRequest): Flow<KycProcess>? {
        //   kycDataRequest.verify()
        return kycProcessRepository.findAllKycProcess(kycDataRequest.userId, kycDataRequest.step, kycDataRequest.status, kycDataRequest.offset!!, kycDataRequest.size!!, PageRequest.of(kycDataRequest.offset!!, kycDataRequest.size!!, Sort.by(Sort.Direction.DESC, "id")))
                ?.map { d ->
                    d.convert(KycProcess::class.java)
                }
    }

    override suspend fun getStepData(stepId: String, userId: String?): Flow<KycProcessDetail>? {
        return kycProcessRepository.findByStepIdOrReferenceId(stepId, stepId, userId)
                ?.map { d ->
                    d.convert(KycProcessDetail::class.java)
                }
    }

    override suspend fun userLevelHistory(userId: String): Flow<UserLevelHistory>? {
        return userStatusHistoryRepository.findAllByUserId(userId)?.map { d -> d.convert(UserLevelHistory::class.java) }
    }


}