package co.nilin.opex.wallet.ports.kafka.listener.model

data class ProfileUpdatedEvent(
    var userId: String,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var mobile: String? = null
) : AuthEvent()
