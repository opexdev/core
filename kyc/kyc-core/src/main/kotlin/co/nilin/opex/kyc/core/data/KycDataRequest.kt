package co.nilin.opex.kyc.core.data

data class KycDataRequest(
        var userId: String?,
        var step: KycStep?,
        var status: KycStatus?,
        var offset:Int?,
        var size:Int?
)