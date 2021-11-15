package co.nilin.opex.matching.engine.core.model

interface Order {
    fun id(): Long?
    fun persistent(): PersistentOrder
}
