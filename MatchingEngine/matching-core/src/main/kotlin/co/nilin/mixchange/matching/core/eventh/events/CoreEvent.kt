package co.nilin.mixchange.matching.core.eventh.events

import co.nilin.mixchange.matching.core.model.Pair
import java.time.LocalDateTime

open class CoreEvent {
    lateinit var pair: Pair
    var eventDate: LocalDateTime = LocalDateTime.now()
}