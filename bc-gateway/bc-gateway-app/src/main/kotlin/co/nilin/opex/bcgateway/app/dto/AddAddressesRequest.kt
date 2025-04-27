package co.nilin.opex.bcgateway.app.dto

data class AddAddressesRequest(
    val addresses: List<String>,
    val memos: List<String?>?,
    val addressType: String,
)