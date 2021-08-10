package co.nilin.opex.matching.core.eventh.events

import co.nilin.opex.matching.core.model.Pair
import java.time.LocalDateTime

open class CoreEvent {
    lateinit var pair: Pair
    var eventDate: LocalDateTime = LocalDateTime.now()
}