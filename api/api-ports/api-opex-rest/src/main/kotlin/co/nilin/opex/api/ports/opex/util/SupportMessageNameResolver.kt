package co.nilin.opex.api.ports.opex.util

import co.nilin.opex.api.core.inout.MessageDto
import co.nilin.opex.api.core.inout.TicketDetailDto
import co.nilin.opex.api.ports.opex.service.OwnerNameResolver

suspend fun OwnerNameResolver.withSenderNames(token: String, messages: List<MessageDto>): List<MessageDto> {
    if (messages.isEmpty()) return messages
    val uuids = messages.map { it.senderId }.toSet()
    val nameMap = resolve(token, uuids)
    return messages.map { it.copy(senderName = nameMap[it.senderId]) }
}

suspend fun OwnerNameResolver.withSenderNames(token: String, message: MessageDto): MessageDto {
    val nameMap = resolve(token, setOf(message.senderId))
    return message.copy(senderName = nameMap[message.senderId])
}

suspend fun OwnerNameResolver.withSenderNames(token: String, ticket: TicketDetailDto): TicketDetailDto =
    ticket.copy(messages = withSenderNames(token, ticket.messages))
