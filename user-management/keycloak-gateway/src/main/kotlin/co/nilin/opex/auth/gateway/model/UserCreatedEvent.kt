package co.nilin.opex.auth.gateway.model

class UserCreatedEvent : AuthEvent {
    lateinit var uuid: String
    var firstName: String? = null
    var lastName: String? = null
    lateinit var email: String


    constructor(uuid: String, firstName: String?, lastName: String?, email: String) : super() {
        this.uuid = uuid
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }
    constructor() : super()

    override fun toString(): String {
        return "UserCreatedEvent(uuid='$uuid', firstName='$firstName', lastName='$lastName', email='$email')"
    }

}