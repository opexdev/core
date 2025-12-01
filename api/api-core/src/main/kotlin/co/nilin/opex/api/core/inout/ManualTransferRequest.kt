package co.nilin.opex.api.core.inout

data class ManualTransferRequest(
    var ref: String,
    var description: String? = null,
    var attachment: String?,
    var gatewayUuid: String
)
