package co.nilin.opex.auth.gateway.data

data class RegisterUserRequest(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val username: String?
) {
    fun isValid(): Boolean {
        return !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && !email.isNullOrEmpty() && !username.isNullOrEmpty()
    }
}