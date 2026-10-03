package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux

interface SupportProxy {

    // User
    suspend fun createTicket(token: String, subjectCode: String, message: String, files: Flux<FilePart>): TicketDetailDto
    suspend fun getUserTickets(token: String, offset: Int, limit: Int): TicketListResponse
    suspend fun getTicket(token: String, ticketId: String): TicketDetailDto
    suspend fun addUserMessage(token: String, ticketId: String, body: String, files: Flux<FilePart>): MessageDto
    suspend fun closeTicket(token: String, ticketId: String): TicketDetailDto
    suspend fun getSubjects(token: String, language: String?): TicketSubjectsResponse

    // Admin
    suspend fun getAdminTickets(
        token: String,
        status: ConversationStatus?,
        userId: String?,
        offset: Int,
        limit: Int
    ): AdminTicketListResponse

    suspend fun getAdminTicket(token: String, ticketId: String): TicketDetailDto
    suspend fun addAgentMessage(token: String, ticketId: String, body: String, files: Flux<FilePart>): MessageDto
    suspend fun closeAdminTicket(token: String, ticketId: String): TicketDetailDto
}
