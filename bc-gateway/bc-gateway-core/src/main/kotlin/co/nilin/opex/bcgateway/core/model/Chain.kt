package co.nilin.opex.bcgateway.core.model

data class Chain(
    val name: String,
    val addressTypes: List<AddressType>,
    val externalChinScannerUrl: String? = null,
    val addressRegx: String? = null
)
