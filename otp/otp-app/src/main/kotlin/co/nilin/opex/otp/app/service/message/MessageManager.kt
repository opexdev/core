package co.nilin.opex.otp.app.service.message

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.model.OTPConfig
import co.nilin.opex.otp.app.model.OTPType
import org.springframework.stereotype.Component

@Component
class MessageManager(
    private val smsSender: SMSSender,
    private val emailSender: EmailSender
) {

    private val logger by LoggerDelegate()

    suspend fun sendMessage(config: OTPConfig, otpType: OTPType, code: String, receiver: String) {
        val message = String.format(config.messageTemplate, code)
        logger.info("$message -> $receiver")
        val result = getSender(otpType).send(receiver, message)
        if (!result)
            throw OpexError.UnableToSendOTP.exception()
    }

    suspend fun getSender(type: OTPType): MessageSender {
        return when (type) {
            OTPType.SMS -> smsSender
            OTPType.EMAIL -> emailSender
            OTPType.COMPOSITE -> throw IllegalStateException("Composite sender not supported")
        }
    }
}