package co.nilin.opex.kyc.core.spi

import co.nilin.opex.kyc.core.data.KycDataRequest
import co.nilin.opex.kyc.core.data.KycProcess
import co.nilin.opex.kyc.core.data.KycResponse
import co.nilin.opex.kyc.core.data.KycRequest
import kotlinx.coroutines.flow.Flow

interface KYCPersister {
    suspend fun kycProcess(kycRequest: KycRequest): KycResponse?

    suspend fun getData(kycDataRequest: KycDataRequest): Flow<KycProcess>?

}