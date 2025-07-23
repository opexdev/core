package co.nilin.opex.otp.app.service.message

import co.nilin.opex.common.utils.LoggerDelegate
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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
    private val from: String
) : MessageSender {

    private val logger by LoggerDelegate()

    override suspend fun send(receiver: String, message: String, metadata: Map<String, Any>): Boolean {
        val subject = "Your otp code"

        val properties = System.getProperties()
        properties.setProperty("mail.smtp.host", host)
        properties["mail.smtp.port"] = port
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.from"] = from
        properties["mail.smtp.ssl.protocols"] = "TLSv1.2"
        val session = Session.getDefaultInstance(properties)

        return try {
            val msg = MimeMessage(session).apply {
                setSubject(subject)
                setFrom(InternetAddress(this@EmailSender.from))
                addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
                setContent(message, "text/html; charset=utf-8")
            }

            with(session.getTransport("smtp")) {
                connect(host, port.toInt(), username, password)
                sendMessage(msg, msg.allRecipients)
                close()
            }

            logger.info("Successfully sent email message")
            true
        } catch (e: Exception) {
            logger.error("Failed to send email message", e)
            false
        }
    }
}