package co.nilin.mixchange.matching.core.model

interface Order{
    fun id():Long?
    fun persistent():PersistentOrder
}
