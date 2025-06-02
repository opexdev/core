package co.nilin.opex.wallet.ports.kafka.listener.model

data class UserCreatedEvent(
    val uuid: String,
    val username: String,
    val email: String?,
    val mobile: String?,
    val firstName: String?,
    val lastName: String?
) : AuthEvent()

