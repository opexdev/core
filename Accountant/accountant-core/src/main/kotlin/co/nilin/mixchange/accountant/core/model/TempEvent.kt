package co.nilin.mixchange.accountant.core.model

import co.nilin.mixchange.matching.core.eventh.events.CoreEvent
import java.time.LocalDateTime

data class TempEvent(val id: Long, val ouid: String, val eventBody: CoreEvent, val eventDate:
LocalDateTime)