package co.nilin.opex.auth.model

data class UpdateMobileRequest(
    val userId :String,
    val mobile: String
)

data class UpdateEmailRequest(
    val userId :String,
    val email: String
)

data class UpdateNameRequest(
    val userId :String,
    val firstName: String,
    val lastName: String
)