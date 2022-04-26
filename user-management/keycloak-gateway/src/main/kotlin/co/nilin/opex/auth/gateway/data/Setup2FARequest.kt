package co.nilin.opex.auth.gateway.data

class Setup2FARequest {

    var secret: String? = null
    var initialCode: String? = null

    constructor()

    constructor(secret: String?, initialCode: String?) {
        this.secret = secret
        this.initialCode = initialCode
    }

}