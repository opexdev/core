package co.nilin.opex.auth.gateway.data

class KYCStatusResponse {

    var status: KYCStatus? = null

    constructor()

    constructor(status: KYCStatus?) {
        this.status = status
    }

}