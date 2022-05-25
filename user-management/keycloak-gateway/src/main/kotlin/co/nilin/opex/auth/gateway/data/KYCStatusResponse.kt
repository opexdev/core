package co.nilin.opex.auth.gateway.data

class KYCStatusResponse {

    var status: KYCStatus? = null
    var rejectReason: String? = null

    constructor()

    constructor(status: KYCStatus?, rejectReason: String?) {
        this.status = status
        this.rejectReason = rejectReason
    }

}