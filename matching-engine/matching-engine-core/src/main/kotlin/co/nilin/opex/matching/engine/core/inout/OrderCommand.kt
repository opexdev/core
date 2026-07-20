package co.nilin.opex.matching.engine.core.inout
import co.nilin.opex.matching.engine.core.model.*

sealed interface OrderCommand {
    val ouid: String
    val uuid: String
    val pair: Pair
}