package co.nilin.opex.profile.core.data.event

data class ProfileUpdatedEvent(
    var userId: String,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var mobile: String? = null
)
