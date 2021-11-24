package co.nilin.opex.accountant.core.model

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import java.time.LocalDateTime

data class TempEvent(val id: Long, val ouid: String, val eventBody: CoreEvent, val eventDate: LocalDateTime)