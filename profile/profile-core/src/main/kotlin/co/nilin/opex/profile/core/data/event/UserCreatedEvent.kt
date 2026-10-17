package co.nilin.opex.profile.core.data.event

import co.nilin.opex.profile.core.data.profile.PersonType
import java.time.LocalDateTime

class UserCreatedEvent {
    var eventDate: LocalDateTime = LocalDateTime.now()
    lateinit var uuid: String
    lateinit var username: String
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var mobile: String? = null
    var personType: PersonType = PersonType.NATURAL


    constructor(
        uuid: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        mobile: String?,
        personType: PersonType = PersonType.NATURAL
    ) : super() {
        this.uuid = uuid
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.mobile = mobile
        this.personType = personType
    }

    constructor() : super()

    override fun toString(): String {
        return "UserCreatedEvent(uuid='$uuid', firstName='$firstName', lastName='$lastName', email='$email' , mobile='$mobile', personType=$personType)"
    }
}