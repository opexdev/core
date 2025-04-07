package co.nilin.opex.otp.app.service.message

interface MessageSender {

    suspend fun send(receiver: String, message: String, metadata: Map<String, Any> = emptyMap())
}