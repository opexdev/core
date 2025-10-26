package co.nilin.opex.profile.core.data.inquiry

data class IbanInfoResponse(
    val ibanInfo: IbanInfo? = null,
    val code: String? = null,
    val message: String? = null
) {
    fun isError() = !code.isNullOrEmpty()
}

data class IbanInfo(
    val bank: String,
    val depositNumber: String,
    val iban: String,
)