package co.nilin.opex.otp.app.proxy

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.data.SMSProviderType
import co.nilin.opex.otp.app.repository.SMSProviderRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SMSTOProxy(
    private val webClient: WebClient,
    private val smsProviderRepository: SMSProviderRepository,
) : SMSProvider {

    override val type = SMSProviderType.SMSTO

    private val logger by LoggerDelegate()

    override suspend fun send(
        receiver: String,
        message: String,
    ): Boolean {

        val config = smsProviderRepository.findById(type.name)
            ?: throw OpexError.UnableToSendOTP.exception()

        val request = SMSRequest(
            to = receiver,
            message = message,
            sender_id = config.sender
        )

        return try {
            val response = webClient.post()
                .uri("${config.baseUrl}/sms/send")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${config.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<String>()
                .awaitSingleOrNull()

            logger.debug("Message sent to receiver $receiver.\n$response")
            true
        } catch (e: Exception) {
            logger.error("Failed to send SMS", e)
            false
        }
    }

    data class SMSRequest(
        val to: String,
        val message: String,
        val sender_id: String? = null,
    )
}