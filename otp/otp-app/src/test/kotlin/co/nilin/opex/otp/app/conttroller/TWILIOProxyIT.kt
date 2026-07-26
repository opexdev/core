package co.nilin.opex.otp.app.conttroller

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.proxy.SMSProvider
import co.nilin.opex.otp.app.proxy.SMSTOProxy
import co.nilin.opex.otp.app.proxy.TWILIOProxy
import co.nilin.opex.otp.app.repository.SMSProviderRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono


class TWILIOProxyIT{
    private val repository = Mockito.mock(SMSProviderRepository::class.java)

    private val webClient = WebClient.builder().build()

    private val proxy = TWILIOProxy(
        webClient = webClient,
        smsProviderRepository = repository
    )
    @Test
    @Disabled("Manual test: requires real apiKey")
    fun sendRealSms(): Unit = runBlocking {

        Mockito.`when`(
            repository.findById(SMSProviderType.TWILIO.name)
        ).thenReturn(
            co.nilin.opex.otp.app.model.SMSProvider(
                id = SMSProviderType.TWILIO.name,
                enabled = true,
                baseUrl = "https://api.twilio.com",
                apiKey = "",
                template = null,
                username = "",
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