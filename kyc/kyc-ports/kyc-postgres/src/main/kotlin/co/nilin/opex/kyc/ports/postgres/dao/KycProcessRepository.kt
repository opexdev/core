package co.nilin.opex.kyc.ports.postgres.dao

import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.ports.postgres.model.base.KycProcess
import co.nilin.opex.kyc.ports.postgres.model.entity.KycProcessModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface KycProcessRepository : ReactiveCrudRepository<KycProcessModel,Long> {

    fun findByUserId(userId:String): Flow<KycProcessModel>?
    fun findByUserIdAndStep(userId:String,kycStep: KycStep): Flow<KycProcessModel>?

    fun findByUserIdOrderByCreateDateDesc(userId:String): Flow<KycProcessModel>?

}