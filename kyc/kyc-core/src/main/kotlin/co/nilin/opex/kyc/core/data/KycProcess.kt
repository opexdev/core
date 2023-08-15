package co.nilin.opex.kyc.core.data

import co.nilin.opex.kyc.core.data.KycStatus
import co.nilin.opex.kyc.core.data.KycStep
import java.time.LocalDateTime

open class KycProcess {
    lateinit var stepId: String
    lateinit var userId: String
    var issuer: String? = null
    var step: KycStep? = null
    var status: KycStatus? = null
    var createDate: LocalDateTime? = LocalDateTime.now()
    var description: String? = null
    var input: String? = null
    var data: ArrayList<String>? = null
    var relatedStep: List<KycProcess>?=null
    var referenceId:String?=null

}