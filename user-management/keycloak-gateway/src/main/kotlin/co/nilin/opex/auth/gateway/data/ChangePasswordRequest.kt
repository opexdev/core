package co.nilin.opex.auth.gateway.data

class ChangePasswordRequest {

    var password: String? = null
    var newPassword: String? = null
    var confirmation: String? = null

    constructor()

    constructor(password: String?, newPassword: String?, confirmation: String?) {
        this.password = password
        this.newPassword = newPassword
        this.confirmation = confirmation
    }

}