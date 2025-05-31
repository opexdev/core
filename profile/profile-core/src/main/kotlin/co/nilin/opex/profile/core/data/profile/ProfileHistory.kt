package co.nilin.opex.profile.core.data.profile

import co.nilin.opex.profile.core.data.kyc.KycLevel
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProfileHistory(
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
    var gender: Boolean? = null,
    var birthDate: LocalDateTime? = null,
    var status: UserStatus? = null,
    var createDate: LocalDateTime? = null,
    var lastUpdateDate: LocalDateTime? = null,
    var creator: String? = null,
    var issuer: String?,
    var changeRequestDate: LocalDateTime?,
    var changeRequestType: String?,
    var updatedItem: List<String>?,
    var kycLevel: KycLevel? = null,
    var verificationStatus : Boolean? = false

)
