package co.nilin.opex.otp.app.service.message

import co.nilin.opex.otp.app.proxy.KaveNegarProxy
import org.springframework.stereotype.Component

@Component
class SMSSender(private val smsProxy: KaveNegarProxy) : MessageSender {

    override suspend fun send(receiver: String, message: String, metadata: Map<String, Any>) {
        smsProxy.send(receiver, message)
    }
}