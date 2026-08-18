package co.nilin.opex.auth.model

data class SetupTOTPRequest(
    val userId: String,
    val label: String?
)

data class SetupTOTPResponse(
    val uri: String
)

data class VerifyTOTPRequest(
    val userId: String,
    val code: String
)

data class VerifyTOTPResponse(val result: Boolean)

data class TOTPQueryResponse(
    val userId: String,
    val isEnabled: Boolean,
    val isActivated: Boolean,
    val uri : String
)

data class TOTPCode(
    val code: String
)
