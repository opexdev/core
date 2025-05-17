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
        if (config.isActivated) {
            val result = getSender(otpType).send(receiver, message)
            if (!result)
                throw OpexError.UnableToSendOTP.exception()
        } else {
            logger.warn("OTP for type ${otpType.name} is not activated. Message will not be sent. $message -> $receiver")
        }
    }

    suspend fun getSender(type: OTPType): MessageSender {
        return when (type) {
            OTPType.SMS -> smsSender
            OTPType.EMAIL -> emailSender
            OTPType.COMPOSITE -> throw IllegalStateException("Composite sender not supported")
        }
    }
}