package co.nilin.opex.admin.core.data

import co.nilin.opex.kyc.core.data.KycProcess
import co.nilin.opex.kyc.core.data.KycResponse
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.profile.core.data.limitation.Limitation
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedAccountResponse
import co.nilin.opex.profile.core.data.profile.Gender
import co.nilin.opex.profile.core.data.profile.KycLevel
import co.nilin.opex.profile.core.data.profile.UserStatus
import java.time.LocalDateTime

data class ProfileResponse(var email:String?,
                           var userId:String?,
                           var firstName: String?=null,
                           var lastName: String?=null,
                           var address:String?=null,
                           var mobile:String?=null,
                           var telephone:String?=null,
                           var postalCode:String?=null,
                           var nationality:String?=null,
                           var identifier:String?=null,
                           var gender: Gender?=null,
                           var birthDate: LocalDateTime?=null,
                           var status: UserStatus?=null,
                           var createDate: LocalDateTime?=null,
                           var lastUpdateDate: LocalDateTime?=null,
                           var creator:String?=null,
                           var kycLevel: KycLevel?=null,
                           var linkedAccounts:List<LinkedAccountResponse>?=null,
                           var limitations:List<Limitation>?=null)
