package co.nilin.opex.captcha.app.service

import co.nilin.opex.captcha.app.api.CaptchaHandler
import co.nilin.opex.captcha.app.extension.sha256
import org.springframework.stereotype.Service

@Service
class CaptchaHandlerImpl : CaptchaHandler {
    override fun generate(): Pair<String, ByteArray> {
        val text = Captcha.generateText()
        val image = Captcha.generateImage(text)
        return text to image
    }

    override fun verify(plainAnswer: String, correctAnswerHash: String): Boolean {
        return plainAnswer.sha256() == correctAnswerHash
    }
}
