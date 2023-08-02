package co.nilin.opex.kyc.core.data

import co.nilin.opex.kyc.core.data.KycLevel

data class ManualUpdateRequest(
         var kycLevel: KycLevel,
) : KycRequest()
