package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

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
    var status: ProfileStatus? = null,
    var createDate: LocalDateTime? = null,
    var lastUpdateDate: LocalDateTime? = null,
    var creator: String? = null,
    var changeRequestDate: LocalDateTime?,
    var changeRequestType: String?,
    var updatedItem: List<String>?,
    var kycLevel: KycLevel? = null,
    var mobileIdentityMatch: Boolean? = null,
    var personalIdentityMatch: Boolean? = null

)
