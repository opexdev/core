package co.nilin.opex.profile.core.data.event

import java.time.LocalDateTime

class UserCreatedEvent {
    var eventDate: LocalDateTime = LocalDateTime.now()
    lateinit var uuid: String
    lateinit var username: String
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var mobile: String? = null


    constructor(uuid: String, firstName: String?, lastName: String?, email: String?, mobile: String?) : super() {
        this.uuid = uuid
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.mobile = mobile
    }

    constructor() : super()

    override fun toString(): String {
        return "UserCreatedEvent(uuid='$uuid', firstName='$firstName', lastName='$lastName', email='$email' , mobile='$mobile')"
    }
}