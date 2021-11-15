package co.nilin.opex.matching.core.model

interface Order {
    fun id(): Long?
    fun persistent(): PersistentOrder
}
