package co.nilin.opex.bcgateway.app.dto

data class AddChainRequest(
    val name: String?,
    val addressType: String?,
    val scheduleDelaySeconds: Int,
    val scheduleErrorDelaySeconds: Int,
    val transactionScannerUrl: String? = null,
    val addressScannerUrl: String? = null
) {
    fun isValid(): Boolean {
        return !name.isNullOrEmpty() && !addressType.isNullOrEmpty() && scheduleDelaySeconds > 0 && scheduleErrorDelaySeconds > 0
    }
}