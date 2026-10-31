package co.nilin.opex.api.core.inout

data class PodiumStatementsRequest(
    val depositNumber: String,
    val startTime: Long,
    val endTime: Long,
    val limit: Int? = null,
    val offset: Int? = null
)
data class FiatScannerResponse(
    val status: Int,
    val body: String
)
