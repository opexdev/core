package co.nilin.opex.profile.core.spi

import co.nilin.opex.profile.core.data.kyc.ManualUpdateRequest

interface KycProxy {
    suspend fun updateKycLevel(updateKycLevelRequest: ManualUpdateRequest)
}