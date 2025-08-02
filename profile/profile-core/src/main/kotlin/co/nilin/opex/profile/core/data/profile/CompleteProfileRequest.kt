package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class CompleteProfileRequest(
    var firstName: String,
    var lastName: String,
    var address: String? = null,
    var telephone: String? = null,
    var postalCode: String? = null,
    var nationality: NationalityType,
    var identifier: String,
    var gender: Gender,
    var birthDate: LocalDateTime,
)
