package co.nilin.opex.captcha.app.service

import co.nilin.opex.captcha.app.api.CaptchaGenerator
import org.springframework.stereotype.Service

@Service
class CaptchaGeneratorImpl : CaptchaGenerator {
    override fun generate(): Pair<String, ByteArray> {
        val text = SimpleCaptcha.generateText()
        val image = SimpleCaptcha.generateImage(text)
        return text to image
    }
}
