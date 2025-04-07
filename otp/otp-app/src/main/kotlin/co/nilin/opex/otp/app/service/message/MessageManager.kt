package co.nilin.opex.otp.app.service.message

import co.nilin.opex.common.OpexError
import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.repository.OTPConfigRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class MessageManager(
    private val smsSender: SMSSender,
    private val emailSender: EmailSender,
    private val configRepository: OTPConfigRepository
) {

    suspend fun sendMessage(otpType: OTPType, code: String, receiver: String) {
        val config = configRepository.findById(otpType).awaitSingleOrNull()
            ?: throw OpexError.OTPConfigNotFound.exception()

        val message = String.format(config.messageTemplate, code)
        getSender(otpType).send(receiver, message)
    }

    suspend fun getSender(type: OTPType): MessageSender {
        return when (type) {
            OTPType.SMS -> smsSender
            OTPType.EMAIL -> emailSender
        }
    }
}