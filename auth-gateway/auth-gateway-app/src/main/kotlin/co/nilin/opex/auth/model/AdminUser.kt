package co.nilin.opex.auth.model

data class CreateLegalRepresentativeRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val mobile: String
)

data class CreateLegalRepresentativeResponse(val uuid: String)

data class SendCredentialsResponse(val emailSent: Boolean)
