package co.nilin.opex.otp.app.conttroller

import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.proxy.SMSTOProxy
import co.nilin.opex.otp.app.repository.SMSProviderRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.reactive.function.client.WebClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled

class SMSTOProxyIT {

    private val repository = Mockito.mock(SMSProviderRepository::class.java)

    private val webClient = WebClient.builder().build()

    private val proxy = SMSTOProxy(
        webClient = webClient,
        smsProviderRepository = repository
    )

    @Test
    @Disabled("Manual test: requires real apiKey")
    fun sendRealSms(): Unit = runBlocking {

        Mockito.`when`(
            repository.findById(SMSProviderType.SMSTO.name)
        ).thenReturn(
            co.nilin.opex.otp.app.model.SMSProvider(
                id = SMSProviderType.SMSTO.name,
                enabled = true,
                baseUrl = "https://api.sms.to",
                apiKey = "",
                template = null,
                username = null,
                password = null,
                sender = "",
                extraConfig = null
            )
        )

        val success = proxy.send(
            receiver = "",
            message = "Manual SMS Test ${System.currentTimeMillis()}"
        )

        assertTrue(success)
    }
}