package co.nilin.opex.otp.app.service.message

import org.springframework.stereotype.Component

@Component
class EmailSender : MessageSender {

    override suspend fun send(receiver: String, message: String, metadata: Map<String, Any>) {
        TODO("Not yet implemented")
    }
}