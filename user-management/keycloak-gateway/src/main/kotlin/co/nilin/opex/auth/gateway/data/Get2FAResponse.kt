package co.nilin.opex.auth.gateway.data

data class Get2FAResponse(val uri: String, val secret: String, val qr:String)