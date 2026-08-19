package co.nilin.opex.api.core.inout

data class ChainInfo(
    val name: String,
    val addressTypes: String?,
    val addressRegex: String? = null,
    val transactionScannerUrl: String? = null,
    val addressScannerUrl: String? = null
)