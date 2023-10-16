package co.nilin.opex.auth.gateway.data

class WhiteListAdaptor {
    var data: MutableList<String?>?=null

    constructor()
    constructor(data: MutableList<String?>) {
        this.data = data
    }
}
