package co.nilin.mixchange.matching.core.eventh.events

interface OneOrderEvent {
    fun ouid(): String
    fun uuid(): String
}