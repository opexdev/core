package co.nilin.mixchange.matching.core.model

enum class OrderDirection {
    ASK, BID
}

enum class MatchConstraint {
    GTC,

    // Immediate or Cancel - equivalent to strict-risk market order
    IOC, // without price cap

    // with price cap
    IOC_BUDGET, // with total amount cap

    // with total amount cap
    // Fill or Kill - execute immediately completely or not at all
    FOK, // without price cap

    // with price cap
    FOK_BUDGET
}

enum class OrderType {
    LIMIT_ORDER, MARKET_ORDER
}
