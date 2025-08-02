package co.nilin.opex.profile.core.data.profile

import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.limitation.Limitation
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedAccountResponse
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

data class Profile(
    var email: String?,
    var userId: String?,
    var firstName: String? = null,
    var lastName: String? = null,
    var address: String? = null,
    var mobile: String? = null,
    var telephone: String? = null,
    var postalCode: String? = null,
    var nationality: String? = null,
    var identifier: String? = null,
    var gender: Gender? = null,
    var birthDate: LocalDateTime? = null,
    var status: UserStatus? = null,
    var createDate: LocalDateTime? = null,
    var lastUpdateDate: LocalDateTime? = null,
    var creator: String? = null,
    var kycLevel: KycLevel? = null,
    var linkedAccounts: List<LinkedAccountResponse>? = null,
    var limitations: List<Limitation>? = null,
    var verificationStatus : Boolean? = false

)
