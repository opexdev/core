package co.nilin.opex.auth.utils

import co.nilin.opex.auth.model.UsernameType
import jakarta.mail.internet.InternetAddress
import java.util.regex.Pattern

object UsernameValidator {

    private val mobileRegex = Pattern.compile("^\\d{10,15}$")

    fun getType(username: String): UsernameType {
        if (isValidEmail(username))
            return UsernameType.EMAIL

        if (isValidMobile(username))
            return UsernameType.MOBILE

        return UsernameType.UNKNOWN
    }

    fun isValidMobile(input: String): Boolean {
        return mobileRegex.matcher(input).matches()
    }

    fun isValidEmail(input: String): Boolean {
        return try {
            InternetAddress(input).validate()
            true
        } catch (e: Exception) {
            false
        }
    }
}