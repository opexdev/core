package co.nilin.opex.api.ports.opex.util

import co.nilin.opex.api.core.inout.AdminTicketListResponse
import co.nilin.opex.api.core.inout.MessageDto
import co.nilin.opex.api.core.inout.TicketDetailDto
import co.nilin.opex.api.ports.opex.service.OwnerNameResolver

suspend fun OwnerNameResolver.withSenderNames(messages: List<MessageDto>): List<MessageDto> {
    if (messages.isEmpty()) return messages
    val uuids = messages.map { it.senderId }.toSet()
    val nameMap = resolve(uuids)
    return messages.map { it.copy(senderName = nameMap[it.senderId]) }
}

suspend fun OwnerNameResolver.withSenderNames(message: MessageDto): MessageDto {
    val nameMap = resolve(setOf(message.senderId))
    return message.copy(senderName = nameMap[message.senderId])
}

suspend fun OwnerNameResolver.withSenderNames(ticket: TicketDetailDto): TicketDetailDto =
    ticket.copy(messages = withSenderNames(ticket.messages))

suspend fun OwnerNameResolver.withOwnerNames(response: AdminTicketListResponse): AdminTicketListResponse {
    if (response.tickets.isEmpty()) return response
    val uuids = response.tickets.map { it.userId }.toSet()
    val nameMap = resolve(uuids)
    return response.copy(tickets = response.tickets.map { it.copy(userFullName = nameMap[it.userId]) })
}
