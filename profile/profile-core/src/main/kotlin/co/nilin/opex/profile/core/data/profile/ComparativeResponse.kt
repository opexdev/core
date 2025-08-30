package co.nilin.opex.profile.core.data.profile

data class ComparativeResponse(
    val firstNameSimilarityPercentage: Int? = null,
    val lastNameSimilarityPercentage: Int? = null,
    val code: String? = null,
    val message: String? = null
) {
    fun isError() = !code.isNullOrEmpty()
}