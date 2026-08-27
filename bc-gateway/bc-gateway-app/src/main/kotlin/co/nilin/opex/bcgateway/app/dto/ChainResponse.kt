package co.nilin.opex.bcgateway.app.dto

data class ChainResponse(
    val name: String,
    val addressTypes: String?,
    val addressRegex: String? = null,
    val transactionScannerUrl: String? = null,
    val addressScannerUrl: String?= null,
)
