package co.nilin.opex.profile.core.data.profile

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Profile(
        var email:String?,
        var userId:String?,
        var firstName: String?=null,
        var lastName: String?=null,
        var address:String?=null,
        var mobile:String?=null,
        var telephone:String?=null,
        var postalCode:String?=null,
        var nationality:String?=null,
        var identifier:String?=null,
        var gender:Boolean?=null,
        var birthDate: LocalDateTime?=null,
        var status: UserStatus?=null,
        var createDate:LocalDateTime?=null,
        var lastUpdateDate:LocalDateTime?=null,
        var creator:String?=null,
        var kycLevel:KycLevel?=null
)
