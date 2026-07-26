package co.nilin.opex.api.core.inout

data class ChainInfo(
    val name: String,
    val addressTypes: String?,
    val externalChainScannerUrl: String? = null,
    val addressRegex: String? = null
)