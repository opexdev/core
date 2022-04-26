package co.nilin.opex.auth.gateway.data

class RegisterUserRequest {

    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var captchaAnswer: String? = null

    constructor()

    constructor(firstName: String?, lastName: String?, email: String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }

    fun isValid(): Boolean {
        return !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && !email.isNullOrEmpty()
    }
}