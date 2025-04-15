package co.nilin.opex.profile.core.data.kyc


import co.nilin.opex.kyc.core.data.KycStep
import java.time.LocalDateTime

open class KycRequest {
    lateinit var userId: String
    var stepId: String? = null
    var referenceId: String? = null
    var issuer: String? = null
    var step: KycStep? = null
    var createDate: LocalDateTime? = LocalDateTime.now()
    var description: String? = null
    var method: KycMethod? = null

}
