package co.nilin.opex.matching.engine.core.eventh.events

interface OneOrderEvent {
    fun ouid(): String
    fun uuid(): String
}