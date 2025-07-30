package co.nilin.opex.profile.ports.inquiry.data

data class ComparativeResponse(
    val firstNameSimilarityPercentage: Int,
    val lastNameSimilarityPercentage: Int,
)