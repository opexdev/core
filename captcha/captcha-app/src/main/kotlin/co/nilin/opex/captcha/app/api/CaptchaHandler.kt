package co.nilin.opex.captcha.app.api

interface CaptchaHandler {
    fun generate(): Pair<String, ByteArray>
    fun verify(plainAnswer: String, correctAnswerHash: String): Boolean
}
