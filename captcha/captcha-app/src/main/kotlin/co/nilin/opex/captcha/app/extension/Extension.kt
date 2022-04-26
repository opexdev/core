package co.nilin.opex.captcha.app.extension

import java.security.MessageDigest

fun String.sha256() = MessageDigest
    .getInstance("SHA-256")
    .digest(this.toByteArray())
    .fold("") { str, it -> str + "%02x".format(it) }
