package co.nilin.opex.auth.service

import co.nilin.opex.common.utils.LoggerDelegate
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmailSender(
    @Value("\${app.mail.host}")
    private val host: String,

    @Value("\${app.mail.port}")
    private val port: String,

    @Value("\${app.mail.username}")
    private val username: String,

    @Value("\${app.mail.password}")
    private val password: String,

    @Value("\${app.mail.from}")
    private val fromAddress: String,

    @Value("\${app.mail.proxy.enabled}")
    private val proxyEnabled: Boolean,

    @Value("\${app.mail.proxy.host}")
    private val proxyHost: String?,

    @Value("\${app.mail.proxy.port}")
    private val proxyPort: String?
) {

    private val logger by LoggerDelegate()

    fun send(receiver: String, subject: String, htmlBody: String): Boolean {
        try {
            if (proxyEnabled) {
                System.setProperty("socksProxyHost", proxyHost)
                System.setProperty("socksProxyPort", proxyPort)
            }

            val props = Properties().apply {
                put("mail.smtp.host", host)
                put("mail.smtp.port", port)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "false")
                put("mail.smtp.starttls.required", "false")
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.from", fromAddress)
                put("mail.smtp.socketFactory.port", port)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")
                put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
            }

            val session = Session.getInstance(props)
            val msg = MimeMessage(session).apply {
                setSubject(subject)
                setFrom(InternetAddress(fromAddress))
                addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
                setContent(htmlBody, "text/html; charset=utf-8")
            }

            session.getTransport("smtp").use { transport ->
                transport.connect(host, port.toInt(), username, password)
                transport.sendMessage(msg, msg.allRecipients)
            }

            logger.info("Successfully sent email to $receiver")
            return true
        } catch (e: Exception) {
            logger.error("Failed to send email to $receiver", e)
            return false
        }
    }
}
