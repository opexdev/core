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
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux

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
        files: Flux<FilePart>
    ): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBody(mapOf("subjectCode" to subjectCode, "message" to message), files)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to create ticket") }
    }

    override suspend fun getUserTickets(token: String, offset: Int, limit: Int): TicketListResponse {
        return webClient.get()
            .uri("$baseUrl/v1/support/tickets") {
                it.queryParam("offset", offset)
                it.queryParam("limit", limit)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketListResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get tickets") }
    }

    override suspend fun getTicket(token: String, ticketId: String): TicketDetailDto {
        return webClient.get()
            .uri("$baseUrl/v1/support/tickets/$ticketId")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get ticket $ticketId") }
    }

    override suspend fun addUserMessage(token: String, ticketId: String, body: String, files: Flux<FilePart>): MessageDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets/$ticketId/messages")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBody(mapOf("body" to body), files)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<MessageDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to add message to ticket $ticketId") }
    }

    override suspend fun closeTicket(token: String, ticketId: String): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/support/tickets/$ticketId/close")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to close ticket $ticketId") }
    }

    override suspend fun getSubjects(token: String, language: String?): TicketSubjectsResponse {
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
        limit: Int
    ): AdminTicketListResponse {
        return webClient.get()
            .uri("$baseUrl/v1/admin/support/tickets") {
                if (status != null) it.queryParam("status", status)
                if (userId != null) it.queryParam("userId", userId)
                it.queryParam("offset", offset)
                it.queryParam("limit", limit)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<AdminTicketListResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get admin tickets") }
    }

    override suspend fun getAdminTicket(token: String, ticketId: String): TicketDetailDto {
        return webClient.get()
            .uri("$baseUrl/v1/admin/support/tickets/$ticketId")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get admin ticket $ticketId") }
    }

    override suspend fun addAgentMessage(token: String, ticketId: String, body: String, files: Flux<FilePart>): MessageDto {
        return webClient.post()
            .uri("$baseUrl/v1/admin/support/tickets/$ticketId/messages")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBody(mapOf("body" to body), files)))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<MessageDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to add agent message to ticket $ticketId") }
    }

    override suspend fun closeAdminTicket(token: String, ticketId: String): TicketDetailDto {
        return webClient.post()
            .uri("$baseUrl/v1/admin/support/tickets/$ticketId/close")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .bodyToMono<TicketDetailDto>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to close admin ticket $ticketId") }
    }
}
