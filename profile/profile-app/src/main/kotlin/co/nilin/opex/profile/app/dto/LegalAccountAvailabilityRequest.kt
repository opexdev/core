package co.nilin.opex.profile.app.dto

data class LegalAccountAvailabilityRequest(
    val nationalCode: String,
    val email: String,
    val mobile: String,
    val companyRegistrationNumber: String,
    val companyNationalId: String
)
