package co.nilin.opex.api.core.inout

import java.time.LocalDateTime

data class TicketSubjectDto(
    val code: String,
    val title: String,
    val description: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)

data class TicketSubjectsResponse(val subjects: List<TicketSubjectDto>)
