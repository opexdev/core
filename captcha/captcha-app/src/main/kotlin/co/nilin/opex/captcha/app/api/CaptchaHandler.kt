package co.nilin.opex.captcha.app.api

interface CaptchaHandler {
    fun generate(): Pair<String, ByteArray>
}
