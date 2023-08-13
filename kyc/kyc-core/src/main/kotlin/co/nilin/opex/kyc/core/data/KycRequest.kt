package co.nilin.opex.kyc.core.data


import java.time.LocalDateTime

 open class KycRequest {
     lateinit var userId: String
     var processId: String? = null
     var issuer: String? = null
     var step: KycStep? = null
     var createDate: LocalDateTime? = LocalDateTime.now()
     var description: String? = null
 }
