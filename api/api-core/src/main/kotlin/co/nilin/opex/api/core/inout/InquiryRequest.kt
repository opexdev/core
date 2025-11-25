package co.nilin.opex.api.core.inout

data class InquiryRequest(
    val startTime: Long,
    val status: List<String>? = null,
)
