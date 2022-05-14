package co.nilin.opex.api.core.inout

enum class OrderDirection {
    ASK, BID
}

enum class MatchConstraint {
    GTC,
    IOC,
    IOC_BUDGET,
    FOK,
    FOK_BUDGET
}

enum class MatchingOrderType {
    LIMIT_ORDER, MARKET_ORDER
}