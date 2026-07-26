package co.nilin.opex.wallet.app.dto

data class ManualTransferRequest(
    var ref: String,
    var description: String? = null,
    var attachment: String?,
    var gatewayUuid: String
)
