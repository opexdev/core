package co.nilin.opex.profile.core.spi

import co.nilin.opex.kyc.core.data.ManualUpdateRequest

interface KycProxy {
    suspend fun updateKycLevel(updateKycLevelRequest: ManualUpdateRequest)
}