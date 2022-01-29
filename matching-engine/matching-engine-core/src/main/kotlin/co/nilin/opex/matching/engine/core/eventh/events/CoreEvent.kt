package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.model.Pair
import java.time.LocalDateTime

open class CoreEvent(
    var pair: Pair,
    var eventDate: LocalDateTime = LocalDateTime.now()
)