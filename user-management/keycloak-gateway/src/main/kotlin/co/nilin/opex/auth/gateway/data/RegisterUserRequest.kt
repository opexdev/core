package co.nilin.opex.auth.gateway.data

class RegisterUserRequest {

    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var username: String? = null
    var captchaAnswer: String? = null

    constructor()

    constructor(firstName: String?, lastName: String?, email: String?, username: String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.username = username
    }

    fun isValid(): Boolean {
        return !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && !email.isNullOrEmpty() && !username.isNullOrEmpty()
    }
}