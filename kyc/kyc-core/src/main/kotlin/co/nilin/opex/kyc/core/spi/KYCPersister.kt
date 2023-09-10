package co.nilin.opex.kyc.core.spi

import co.nilin.opex.kyc.core.data.*
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Mono

interface KYCPersister {
    suspend fun kycProcess(kycRequest: KycRequest): KycResponse?

    suspend fun getSteps(kycDataRequest: KycDataRequest): Flow<KycProcess>?

    suspend fun getStepData(stepId: String, userId: String?): Flow<KycProcessDetail>?

    suspend fun userLevelHistory(userId: String): Flow<UserLevelHistory>?


}