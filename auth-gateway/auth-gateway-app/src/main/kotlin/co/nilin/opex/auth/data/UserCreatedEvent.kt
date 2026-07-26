package co.nilin.opex.auth.data

data class UserCreatedEvent(
    val uuid: String,
    val username: String,
    val email: String?,
    val mobile: String?,
    val firstName: String?,
    val lastName: String?
) : AuthEvent()