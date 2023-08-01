package co.nilin.opex.kyc.core.data

import co.nilin.opex.profile.core.data.profile.KycLevel

data class ManualUpdateRequest(
         var kycLevel: KycLevel,
) : KycRequest()
