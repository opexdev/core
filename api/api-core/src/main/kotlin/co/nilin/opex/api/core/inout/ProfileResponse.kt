package co.nilin.opex.api.core.inout

data class ProfileResponse(
    var email: String?,
    var userId: String?,
    var firstName: String? = null,
    var lastName: String? = null,
    var address: String? = null,
    var mobile: String? = null,
    var telephone: String? = null,
    var postalCode: String? = null,
    var nationality: NationalityType? = null,
    var identifier: String? = null,
    var gender: Gender? = null,
    var birthDate: Long? = null,
    var status: ProfileStatus? = null,
    var createDate: Long? = null,
    var lastUpdateDate: Long? = null,
    var creator: String? = null,
    var kycLevel: KycLevel? = null,
    var mobileIdentityMatch: Boolean? = null,
    var personalIdentityMatch: Boolean? = null

)