package co.nilin.opex.otp.app.service.message

import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getUserLanguage
import co.nilin.opex.common.utils.LoggerDelegate
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.util.*

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
    private val proxyPort: String?,

    @Value("\${app.name}")
    private val appName: String
) : MessageSender {

    private val logger by LoggerDelegate()

    private val templateEn: String by lazy { loadTemplate("templates/otp-email.html") }
    private val templateFa: String by lazy { loadTemplate("templates/otp-email-fa.html") }

    private fun loadTemplate(path: String): String =
        ClassPathResource(path).inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

    override suspend fun send(
        receiver: String,
        message: String,
        metadata: Map<String, Any>
    ): Boolean {

        val subject = "Your otp code"
        val code = metadata["code"]?.toString() ?: message
        val language = UserLanguage.safeValueOf(getUserLanguage().awaitSingleOrNull())
        val template = if (language == UserLanguage.FA) templateFa else templateEn
        val body = template
            .replace("{{OTP}}", code)
            .replace("{{APP_NAME}}", appName)

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
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.from", fromAddress)
                put("mail.smtp.starttls.enable", "false")
                put("mail.smtp.starttls.required", "false")

                put("mail.smtp.socketFactory.port", port)
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")

                put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
            }

            val session = Session.getInstance(props)
            session.debug = true
            val msg = MimeMessage(session).apply {
                setSubject(subject)
                setFrom(InternetAddress(fromAddress))
                addRecipient(Message.RecipientType.TO, InternetAddress(receiver))
                setContent(body, "text/html; charset=utf-8")
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