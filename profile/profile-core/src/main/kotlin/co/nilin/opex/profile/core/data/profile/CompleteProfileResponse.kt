package co.nilin.opex.profile.core.data.profile

import co.nilin.opex.profile.core.data.kyc.KycLevel
import java.time.LocalDateTime

open class CompleteProfileResponse(
    var id: Long,
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
    var kycLevel: KycLevel? = null,
    var mobileIdentityMatch : Boolean? = false,
    var personalIdentityMatch : Boolean? = false
)
