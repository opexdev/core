package co.nilin.opex.auth.gateway.data

class KycRequest {

    var selfiePath: String? = null
    var idCardPath: String? = null
    var acceptFormPath: String? = null

    constructor()

    constructor(selfiePath: String?, idCardPath: String?, acceptFormPath: String?) {
        this.selfiePath = selfiePath
        this.idCardPath = idCardPath
        this.acceptFormPath = acceptFormPath
    }

}