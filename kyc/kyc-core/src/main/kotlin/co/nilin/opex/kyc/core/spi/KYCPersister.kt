package co.nilin.opex.kyc.core.spi

import co.nilin.opex.kyc.core.data.KycResponse
import co.nilin.opex.kyc.core.data.KycRequest

interface KYCPersister {
    suspend fun kycProcess(kycRequest: KycRequest): KycResponse?
}