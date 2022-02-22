package co.nilin.opex.bcgateway.app.dto

data class AddressTypeRequest(
    val name: String?,
    val addressRegex: String?,
    val memoRegex: String?
)