package co.nilin.opex.auth.gateway.data

class WhiteListRequest {
    var data: List<String>?=null

    constructor()
    constructor(data: List<String>?) {
        this.data = data
    }
}
