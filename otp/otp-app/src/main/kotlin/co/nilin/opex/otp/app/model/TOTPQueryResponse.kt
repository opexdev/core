package co.nilin.opex.otp.app.model

data class TOTPQueryResponse(
    val userId: String,
    val isEnabled: Boolean,
    val isActivated: Boolean,
)
