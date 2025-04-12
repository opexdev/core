package co.nilin.opex.otp.app.service.message

import co.nilin.opex.common.utils.LoggerDelegate
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmailSender(
    @Value("\${otp.email.host}")
    private val host: String,
    @Value("\${otp.email.username}")
    private val username: String,
    @Value("\${otp.email.password}")
    private val password: String
) : MessageSender {

    private val logger by LoggerDelegate()

    override suspend fun send(receiver: String, message: String, metadata: Map<String, Any>): Boolean {
        val subject = "Your otp code"
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", "2525")
            put("mail.smtp.auth", "true")
            put("mail.smtp.username", username)
        }

        val auth = object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
        }
        val session = Session.getInstance(props, auth)
        return try {
            val emailMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setSubject(subject)
                setRecipient(Message.RecipientType.TO, InternetAddress(receiver))
                setContent(message, "text/html; charset=utf-8")
            }
            Transport.send(emailMessage)
            logger.info("Successfully sent email message")
            true
        } catch (e: Exception) {
            logger.error("Failed to send email message", e)
            false
        }
    }
}