package co.nilin.opex.auth.gateway.data

class ForgotPasswordRequest {

    var password: String? = null
    var passwordConfirmation: String? = null

    constructor()

    constructor(password: String?, passwordConfirmation: String?) {
        this.password = password
        this.passwordConfirmation = passwordConfirmation
    }

}