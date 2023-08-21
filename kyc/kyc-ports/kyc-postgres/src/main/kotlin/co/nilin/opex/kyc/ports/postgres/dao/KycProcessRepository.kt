package co.nilin.opex.kyc.ports.postgres.dao

import co.nilin.opex.kyc.core.data.KycLevelDetail
import co.nilin.opex.kyc.core.data.KycStatus
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.ports.postgres.model.base.KycProcess
import co.nilin.opex.kyc.ports.postgres.model.entity.KycProcessModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface KycProcessRepository : ReactiveCrudRepository<KycProcessModel,Long> {

    fun findByUserId(userId:String): Flow<KycProcessModel>?
    fun findByUserIdAndStepId(userId:String,stepId: String): Mono<KycProcessModel>?

    fun findByUserIdOrderByCreateDateDesc(userId:String): Flow<KycProcessModel>?


    @Query("select * from kyc_process kp where (:userId is NULL or kp.user_id= :userId)  And (:step is NULL or kp.step=:step) And (:status is NULL or kp.status=:status) OFFSET :offset LIMIT :size; ")
    fun findAllKycProcess(userId:String?,step:KycStep?,status:KycStatus?,offset:Int,size:Int ,pageable: Pageable) : Flow<KycProcessModel>?

    @Query("select * from kyc_process kp where (:userId is NULL or kp.user_id= :userId)  And ( kp.step_id=:stepId) OR ( kp.reference_id=:referenceId)  ")

    fun findByStepIdOrReferenceId(stepId: String,referenceId:String, userId:String?):Flow<KycProcessModel>?

}