package co.nilin.opex.matching.core.eventh.events

interface OneOrderEvent {
    fun ouid(): String
    fun uuid(): String
}