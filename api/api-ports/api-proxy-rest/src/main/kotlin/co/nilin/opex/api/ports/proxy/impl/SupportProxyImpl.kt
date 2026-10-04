package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.SupportProxy
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class SupportProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : SupportProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.support.url}")
    private lateinit var baseUrl: String

    private suspend fun multipartBody(textParts: Map<String, String>, files: Flux<FilePart>): LinkedMultiValueMap<String, Any> {
        val map = LinkedMultiValueMap<String, Any>()
        textParts.forEach { (key, value) -> map.add(key, value) }
        files.asFlow().toList().forEach { map.add("files", it) }
        return map
    }

    override suspend fun createTicket(
        token: String,
        subjectCode: String,
        message: String,
        files: Flux<FilePart>,
        language: String?
    ): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBody(mapOf("subjectCode" to subjectCode, "message" to message), files)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to create ticket") }
    }

    override suspend fun getUserTickets(token: String, offset: Int, limit: Int, language: String?): TicketListResponse {
        return webClient.get()
            .uri("$baseUrl/v1/support/tickets") {
                it.queryParam("offset", offset)
                it.queryParam("limit", limit)
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketListResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get tickets") }
    }

    override suspend fun getTicket(token: String, ticketNumber: String, language: String?): TicketDetailDto {
        return webClient.get()
            .uri("$baseUrl/v1/support/tickets/$ticketNumber") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get ticket $ticketNumber") }
    }

    override suspend fun addUserTicketMessage(token: String, ticketNumber: String, body: String?, files: Flux<FilePart>): MessageDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets/$ticketNumber/messages")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBody(body?.let { mapOf("body" to it) } ?: emptyMap(), files)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<MessageDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to add message to ticket $ticketNumber") }
    }

    override suspend fun closeTicket(token: String, ticketNumber: String, language: String?): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets/$ticketNumber/close") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to close ticket $ticketNumber") }
    }

    override suspend fun rateTicket(token: String, ticketNumber: String, rating: Int, language: String?): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets/$ticketNumber/rating") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(RateTicketRequest(rating)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to rate ticket $ticketNumber") }
    }

    override suspend fun getTicketSubjects(token: String, language: String?): TicketSubjectsResponse {
        return webClient.get()
            .uri("$baseUrl/v1/support/subjects") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketSubjectsResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get support subjects") }
    }

    override suspend fun getAdminTickets(
        token: String,
        status: ConversationStatus?,
        userId: String?,
        offset: Int,
        limit: Int,
        language: String?,
        ticketNumber: String?,
        subjectCode: String?
    ): AdminTicketListResponse {
        return webClient.get()
            .uri("$baseUrl/v1/admin/support/tickets") {
                if (status != null) it.queryParam("status", status)
                if (userId != null) it.queryParam("userId", userId)
                it.queryParam("offset", offset)
                it.queryParam("limit", limit)
                if (language != null) it.queryParam("language", language)
                if (ticketNumber != null) it.queryParam("ticketNumber", ticketNumber)
                if (subjectCode != null) it.queryParam("subjectCode", subjectCode)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<AdminTicketListResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get admin tickets") }
    }

    override suspend fun getAdminTicket(token: String, ticketNumber: String, language: String?): TicketDetailDto {
        return webClient.get()
            .uri("$baseUrl/v1/admin/support/tickets/$ticketNumber") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get admin ticket $ticketNumber") }
    }

    override suspend fun addAgentTicketMessage(token: String, ticketNumber: String, body: String?, files: Flux<FilePart>): MessageDto {
        return webClient.post()
            .uri("$baseUrl/v1/admin/support/tickets/$ticketNumber/messages")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBody(body?.let { mapOf("body" to it) } ?: emptyMap(), files)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<MessageDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to add agent message to ticket $ticketNumber") }
    }

    override suspend fun closeAdminTicket(token: String, ticketNumber: String, language: String?): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/admin/support/tickets/$ticketNumber/close") {
                if (language != null) it.queryParam("language", language)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to close admin ticket $ticketNumber") }
    }
}
