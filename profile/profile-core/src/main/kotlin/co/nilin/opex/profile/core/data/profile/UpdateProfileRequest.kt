package co.nilin.opex.profile.core.data.profile

import java.time.LocalDateTime

data class UpdateProfileRequest(
    var firstName: String? = null,
    var lastName: String? = null,
    var address: String? = null,
    var telephone: String? = null,
    var postalCode: String? = null,
    var nationality: String? = null,
    var identifier: String? = null,
    var gender: Gender? = null,
    val mobile: String? = null,
    var birthDate: LocalDateTime? = null,
)
