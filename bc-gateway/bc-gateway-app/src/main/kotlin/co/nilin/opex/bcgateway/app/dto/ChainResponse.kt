package co.nilin.opex.bcgateway.app.dto

data class ChainResponse(
    val name: String,
    val addressTypes: String?,
    val externalChainScannerUrl: String? = null,
    val addressRegex: String? = null
)
