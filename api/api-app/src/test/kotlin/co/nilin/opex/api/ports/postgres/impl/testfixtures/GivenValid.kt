package co.nilin.opex.api.ports.postgres.impl.testfixtures

import co.nilin.opex.api.core.event.RichOrder
import co.nilin.opex.api.core.event.RichOrderUpdate
import co.nilin.opex.api.core.event.RichTrade
import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import co.nilin.opex.api.ports.postgres.model.TradeModel
import java.math.BigDecimal
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object Valid {
    val PRINCIPAL = Principal { "98c7ca9b-2d9c-46dd-afa8-b0cd2f52a97c" }

    val MAKER_ORDER_MODEL = OrderModel(
        1,
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        PRINCIPAL.name,
        "id", // ?
        "ETH_USDT",
        1,
        0.01,
        0.01,
        0.0001,
        0.01,
        "1",
        OrderDirection.ASK,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        100000.0,
        0.01,
        0.0, // ?
        LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
        LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
    )

    val TAKER_ORDER_MODEL = OrderModel(
        2,
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        PRINCIPAL.name,
        "id", // ?
        "ETH_USDT",
        1,
        0.01,
        0.01,
        0.0001,
        0.01,
        "1",
        OrderDirection.ASK,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        100000.0,
        0.01,
        0.0, // ?
        LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
        LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
    )

    val ORDER_STATUS_MODEL = OrderStatusModel(
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        0.0, // ?
        0.0, // ?
        0, // ?
        0, // ?
        LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
    )

    val SYMBOL_MAP_MODEL = SymbolMapModel(
        1,
        "ETH_USDT",
        "binance",
        "ETHUSDT"
    )

    val TRADE_MODEL = TradeModel(
        1,
        1,
        "ETH_USDT",
        0.001,
        100000.0,
        100000.0,
        0.001,
        0.001,
        "",
        "",
        LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC),
        "99289106-2775-44d4-bffc-ca35fc25e58c",
        "2fa73fa2-6d70-44b8-8571-e2b24e2eea2b",
        "52c6d890-3dd4-4fa8-9425-d9e0d6274705",
        "07bb979a-dfca-475b-a38b-fcc5dd2f88d8",
        LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC)
    )

    val QUERY_ORDER_RESPONSE = QueryOrderResponse(
        "ETH_USDT",
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        1,
        1, // ?,
        "id",
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(100),
        BigDecimal.valueOf(1),
        OrderStatus.FILLED,
        TimeInForce.GTC,
        OrderType.LIMIT,
        OrderSide.BUY,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(100000),
        Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        true,
        BigDecimal.valueOf(0)
    )

    val AGGREGATED_ORDER_PRICE_MODEL = AggregatedOrderPriceModel(
        100000.0,
        0.001
    )

    val ORDER_BOOK_RESPONSE = OrderBookResponse(
        BigDecimal.valueOf(100000.0),
        BigDecimal.valueOf(0.001)
    )

    val RICH_ORDER = RichOrder(
        null,
        "ETH_USDT",
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        "18013d13-0568-496b-b93b-2524c8132b93",
        "1",
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0.0001),
        BigDecimal.valueOf(0.01),
        OrderDirection.ASK,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        BigDecimal.valueOf(1000001),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0), // ?
        BigDecimal.valueOf(0), // ?
        BigDecimal.valueOf(0), // ?
        0
    )

    val RICH_ORDER_UPDATE = RichOrderUpdate(
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        BigDecimal.valueOf(1000001),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0.08),
        OrderStatus.PARTIALLY_FILLED
    )

    val RICH_TRADE = RichTrade(
        1, // ?
        "ETH_USDT",
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        "18013d13-0568-496b-b93b-2524c8132b93",
        1,
        OrderDirection.ASK,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0), // ?
        BigDecimal.valueOf(0), // ?
        BigDecimal.valueOf(0), // ?
        "", // ?
        "26931efc-891b-4599-9921-1d265829b410",
        "5296a097-6478-464f-91a6-5c434ac4207d",
        2,
        OrderDirection.ASK,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0), // ?
        BigDecimal.valueOf(0), // ?
        BigDecimal.valueOf(0), // ?
        "", // ?
        BigDecimal.valueOf(0), // ?
        LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC)
    )

    val ALL_ORDER_REQUEST = AllOrderRequest(
        "ETH_USDT",
        Date.from(LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        500
    )

    val TRADE_REQUEST = TradeRequest(
        "ETH_USDT",
        1,
        Date.from(LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        500
    )

    val MARKET_TRADE_RESPONSE = MarketTradeResponse(
        "ETH_USDT",
        1,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(0.001),
        Date.from(LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
        isBestMatch = true,
        isMakerBuyer = true
    )

    val QUERY_ORDER_REQUEST = QueryOrderRequest(
        "ETH_USDT",
        1,
        "2" // ?
    )
}