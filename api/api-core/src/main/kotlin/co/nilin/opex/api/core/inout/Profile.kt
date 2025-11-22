package co.nilin.opex.api.core.inout

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
    var nationality: NationalityType? = null,
    var identifier: String? = null,
    var gender: Gender? = null,
    var birthDate: LocalDateTime? = null,
    var status: ProfileStatus? = null,
    var createDate: LocalDateTime? = null,
    var lastUpdateDate: LocalDateTime? = null,
    var creator: String? = null,
    var kycLevel: KycLevel? = null,
    var mobileIdentityMatch: Boolean? = null,
    var personalIdentityMatch: Boolean? = null


)

enum class NationalityType {
    IRANIAN,
    NON_IRANIAN
}

enum class Gender {
    FEMALE, MALE
}

enum class ProfileStatus {
    CREATED,
    CONTACT_INFO_COMPLETED,
    PROFILE_COMPLETED,
    SYSTEM_APPROVED,
    PENDING_ADMIN_APPROVAL,
    ADMIN_REJECTED,
    ADMIN_APPROVED
}