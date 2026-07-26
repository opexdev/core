package co.nilin.opex.otp.app.service.message

import co.nilin.opex.otp.app.proxy.KaveNegarProxy
import co.nilin.opex.otp.app.service.SMSProviderRouter
import org.springframework.stereotype.Component

@Component
class SMSSender(
    private val router: SMSProviderRouter
) : MessageSender {
    override suspend fun send(receiver: String, message: String, metadata: Map<String, Any>): Boolean {
        val provider = router.getProvider(receiver)
        return provider.send(receiver, message)
    }
}