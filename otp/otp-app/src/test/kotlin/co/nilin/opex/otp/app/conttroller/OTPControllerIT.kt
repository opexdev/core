package co.nilin.opex.otp.app.conttroller


import co.nilin.opex.otp.app.service.message.EmailSender
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class EmailSenderIT {
    @Disabled("Manual test: requires real SMTP + proxy")
    @Test
    fun `should send email via real smtp`() = runBlocking {

        val sender = EmailSender(
            host = requireEnv("SMTP_HOST"),
            port = requireEnv("SMTP_PORT"),
            username = requireEnv("SMTP_USER"),
            password = requireEnv("SMTP_PASS"),
            from = requireEnv("SMTP_FROM"),
            proxyIsEnabled = true,
            proxyHost = requireEnv("SMTP_SOCKS_HOST"),
            proxyPort = requireEnv("SMTP_SOCKS_PORT")
        )

        val result = sender.send(
            receiver = requireEnv("SMTP_RECEIVER"),
            message = "<h1>Integration Test</h1>",
            metadata = emptyMap()
        )

        Assertions.assertTrue(result)
    }

    private fun requireEnv(key: String): String =
        System.getenv(key) ?: error("Missing env: $key")
}