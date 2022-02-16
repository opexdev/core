package co.nilin.opex.admin.core.events

data class CreateOrderBookEvent(
    val leftSideName: String,
    val rightSideName: String,

    val pairName: String = "${leftSideName}_${rightSideName}"
)