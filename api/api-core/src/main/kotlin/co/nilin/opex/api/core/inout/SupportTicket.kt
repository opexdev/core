package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

enum class ConversationStatus {
    WAITING_FOR_ADMIN, WAITING_FOR_USER, CLOSED
}

enum class SenderType {
    USER, ADMIN
}

data class AttachmentDto(
    val id: Long,
    val fileName: String,
    val contentType: String,
    val key: String,
    val createdAt: LocalDateTime
)

data class MessageDto(
    val id: Long,
    val senderType: SenderType,
    val senderId: String,
    val body: String,
    val createdAt: LocalDateTime,
    val attachments: List<AttachmentDto>,
    val senderName: String? = null
)

data class TicketDetailDto(
    val id: Long,
    val ticketNumber: String,
    val subject: String,
    val userId: String,
    val status: ConversationStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val closedAt: LocalDateTime?,
    val rating: Int?,
    val ratedAt: LocalDateTime?,
    val messages: List<MessageDto>
)

data class TicketSummaryDto(
    val id: Long,
    val ticketNumber: String,
    val subject: String,
    val status: ConversationStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val rating: Int?,
    val ratedAt: LocalDateTime?
)

data class AdminTicketSummaryDto(
    val ticketNumber: String,
    val subject: String,
    val userId: String,
    val status: ConversationStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val rating: Int?,
    val ratedAt: LocalDateTime?
)

data class TicketListResponse(val total: Long, val tickets: List<TicketSummaryDto>)

data class AdminTicketListResponse(val total: Long, val tickets: List<AdminTicketSummaryDto>)

data class RateTicketRequest(val rating: Int)
