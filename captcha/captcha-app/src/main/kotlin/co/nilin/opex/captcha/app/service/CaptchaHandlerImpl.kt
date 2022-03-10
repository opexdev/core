package co.nilin.opex.captcha.app.service

import co.nilin.opex.captcha.app.api.CaptchaHandler
import org.springframework.stereotype.Service

@Service
class CaptchaHandlerImpl : CaptchaHandler {
    override fun generate(): Pair<String, ByteArray> {
        val text = Captcha.generateText()
        val image = Captcha.generateImage(text)
        return text to image
    }
}