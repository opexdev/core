package co.nilin.opex.bcgateway.app.dto

data class AddAddressRequest(
    val addresses: List<String>,
    val memos: List<String?>?,
    val addressType: String,
)