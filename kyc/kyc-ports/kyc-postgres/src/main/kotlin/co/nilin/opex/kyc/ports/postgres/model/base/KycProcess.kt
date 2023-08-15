package co.nilin.opex.kyc.ports.postgres.model.base

import co.nilin.opex.kyc.core.data.KycStatus
import co.nilin.opex.kyc.core.data.KycStep
import java.time.LocalDateTime

open class KycProcess {
    lateinit var stepId: String
    lateinit var userId: String
    var issuer: String? = null
    var step: KycStep? = null
    var status: KycStatus?=null
    var createDate:LocalDateTime?= LocalDateTime.now()
    var description:String?=null
    var input:String?=null
    var referenceId:String?=null

}