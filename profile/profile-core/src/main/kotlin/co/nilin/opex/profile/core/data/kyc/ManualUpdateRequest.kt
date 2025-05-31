package co.nilin.opex.profile.core.data.kyc

data class ManualUpdateRequest(
    var level: KycLevelDetail,
) : KycRequest()
