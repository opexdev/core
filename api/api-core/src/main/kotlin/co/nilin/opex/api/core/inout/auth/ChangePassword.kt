package co.nilin.opex.api.core.inout.auth

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirmation: String,
)

data class ChangePasswordResponse(
    val token: String,
    val otp: RequiredOTP,
    val otpCode: String?,
)

data class ConfirmChangePasswordRequest(
    val token: String,
    val otp: String,
)

data class ResendChangePasswordOtpRequest(
    val token: String,
)
