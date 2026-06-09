package co.nilin.opex.otp.app.service.message

import co.nilin.opex.common.utils.LoggerDelegate
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Properties

@Component
class EmailSender(
    @Value("\${otp.email.host}")
    private val host: String,

    @Value("\${otp.email.port}")
    private val port: String,

    @Value("\${otp.email.username}")
    private val username: String,

    @Value("\${otp.email.password}")
    private val password: String,

    @Value("\${otp.email.from}")
    private val fromAddress: String,

    @Value("\${otp.email.proxy.enabled}")
    private val proxyEnabled: Boolean,

    @Value("\${otp.email.proxy.host}")
    private val proxyHost: String?,

    @Value("\${otp.email.proxy.port}")
    private val proxyPort: String?
) : MessageSender {

    private val logger by LoggerDelegate()

    override suspend fun send(
        receiver: String,
        message: String,
        metadata: Map<String, Any>
    ): Boolean {

        val subject = "Your otp code"

        try {
            // 🔥 SOCKS must be JVM-level (NOT JavaMail props)
            if (proxyEnabled) {
                System.setProperty("socksProxyHost", proxyHost)
                System.setProperty("socksProxyPort", proxyPort)
            }

            val props = Properties().apply {
                put("mail.smtp.host", host)
                put("mail.smtp.port", port)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.from", fromAddress)
                put("mail.smtp.ssl.protocols", "TLSv1.2")
            }

            val session = Session.getInstance(props)

            val msg = MimeMessage(session).apply {
                setSubject(subject)
                setFrom(InternetAddress(fromAddress ))
                addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
                setContent(message, "text/html; charset=utf-8")
            }

            session.getTransport("smtp").use { transport ->
                transport.connect(host, port.toInt(), username, password)
                transport.sendMessage(msg, msg.allRecipients)
            }

            logger.info("Successfully sent email message")
            return true

        } catch (e: Exception) {
            logger.error("Failed to send email message", e)
            return false
        }
    }
}