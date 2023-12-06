package co.nilin.opex.bcgateway.app.dto

data class ChainEndpointRequest(
    val url: String,
    val username: String?,
    val password: String?
)