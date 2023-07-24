package co.nilin.opex.profile.ports.postgres.model.base

import co.nilin.opex.profile.core.data.profile.UserStatus
import co.nilin.opex.profile.core.data.profile.RequiredAdminActions
import co.nilin.opex.profile.core.data.profile.RequiredUserActions
import java.time.LocalDateTime

open class Profile {
     lateinit var email: String
     lateinit var userId: String
     var firstName: String? = null
     var lastName: String?=null
     var address: String?=null
     var mobile: String?=null
     var telephone: String?=null
     var postalCode: String?=null
     var nationality: String?=null
     var identifier: String?=null
     var gender: Boolean?=null
     lateinit var birthDate: LocalDateTime
     var status: UserStatus?=null
     var createDate: LocalDateTime?=null
     var lastUpdateDate: LocalDateTime?=null
     var creator:String?=null
     var requiredUserActions : RequiredUserActions?=null
     var requiredAdminActions : RequiredAdminActions?=null
 }