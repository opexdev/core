package co.nilin.opex.otp.app.proxy

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.repository.SMSProviderRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TWILIOProxy(
    private val webClient: WebClient,
    private val smsProviderRepository: SMSProviderRepository,
) : SMSProvider {

    override val type = SMSProviderType.TWILIO

    private val logger by LoggerDelegate()

    override suspend fun send(
        receiver: String,
        message: String,
    ): Boolean {

        val config = smsProviderRepository.findById(type.name)
            ?: throw OpexError.UnableToSendOTP.exception()

        val accountSid = config.username
            ?: throw IllegalStateException("Twilio Account SID is not configured")

        val authToken = config.apiKey
            ?: throw IllegalStateException("Twilio Auth Token is not configured")

        val sender = config.sender
            ?: throw IllegalStateException("Twilio sender number is not configured")

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("To", receiver)
            add("From", sender)
            add("Body", message)
        }

        return try {
            val response = webClient.post()
                .uri("${config.baseUrl}/2010-04-01/Accounts/$accountSid/Messages.json")
                .headers {
                    it.setBasicAuth(accountSid, authToken)
                }
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<String>()
                .awaitSingleOrNull()

            logger.debug("Message sent to receiver $receiver.\n$response")
            true
        } catch (e: WebClientResponseException) {
            logger.error(
                "Twilio error: ${e.statusCode}\n${e.responseBodyAsString}",
                e
            )
            logger.error("Failed to send SMS", e)
            false
        }
    }
}