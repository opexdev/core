package data

import co.nilin.opex.profile.core.data.profile.KYCLevel
import java.time.LocalDateTime

data class UpdateKYCLevelRequest(
        var userId:String,
        var kycLevel: KYCLevel,
        var requestId:String?=null,
        var reason:String?=null,
        var description:String?=null,
        var detail:String?=null,
        var lastUpdateDate:LocalDateTime,
)
