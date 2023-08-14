package co.nilin.opex.kyc.core.data

data class KycDataRequest(
        var userId: String?,
        var processId: String?,
        var step: KycLevelDetail?
)