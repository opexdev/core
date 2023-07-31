package co.nilin.opex.core.spi

import co.nilin.opex.core.data.KycResponse
import co.nilin.opex.core.data.KycRequest

interface KYCPersister {
    suspend fun kycProcess(kycRequest: KycRequest):KycResponse?
}