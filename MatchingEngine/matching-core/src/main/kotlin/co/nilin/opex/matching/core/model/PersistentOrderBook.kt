package co.nilin.opex.matching.core.model

class PersistentOrderBook {

    lateinit var pair: Pair
    var lastOrder: PersistentOrder? = null
    var orders: List<PersistentOrder>? = emptyList()

    constructor() {
    }

    constructor(pair: Pair) {
        this.pair = pair
    }

}