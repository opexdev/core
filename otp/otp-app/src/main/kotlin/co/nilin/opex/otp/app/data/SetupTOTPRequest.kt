package co.nilin.opex.otp.app.data

data class SetupTOTPRequest(
    val userId: String,
    val label: String
)