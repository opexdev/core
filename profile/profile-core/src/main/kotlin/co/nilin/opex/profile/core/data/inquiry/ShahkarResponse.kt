package co.nilin.opex.profile.core.data.inquiry

data class ShahkarResponse(
    val matched: Boolean? = null,
    val code: String? = null,
    val message: String? = null
) {
    fun isError() = !code.isNullOrEmpty()
}