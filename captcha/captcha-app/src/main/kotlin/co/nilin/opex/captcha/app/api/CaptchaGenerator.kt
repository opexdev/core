package co.nilin.opex.captcha.app.api

interface CaptchaGenerator {
    fun generate(): Pair<String, ByteArray>
}
