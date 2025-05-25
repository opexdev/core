package co.nilin.opex.auth.model

import co.nilin.opex.auth.utils.UsernameValidator
import co.nilin.opex.common.OpexError

data class Username(
    val value: String,
    val type: UsernameType
) {

    fun asAttribute() = Attribute(type.name.lowercase(), value)

    companion object {
        fun create(username: String): Username {
            val type = UsernameValidator.getType(username.replace("+", ""))
            if (type.isUnknown()) throw OpexError.InvalidUsername.exception()
            return Username(username, type)
        }
    }
}

enum class UsernameType(val otpType: OTPType) {
    MOBILE(OTPType.SMS),
    EMAIL(OTPType.EMAIL),
    UNKNOWN(OTPType.NONE);

    fun isUnknown() = this == UNKNOWN
}

enum class OTPType {
    EMAIL, SMS, TOTP, NONE
}