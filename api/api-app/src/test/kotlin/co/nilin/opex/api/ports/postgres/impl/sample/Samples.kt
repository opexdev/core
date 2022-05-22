package co.nilin.opex.api.ports.postgres.impl.sample

import co.nilin.opex.api.core.event.RichOrder
import co.nilin.opex.api.core.event.RichOrderUpdate
import co.nilin.opex.api.core.event.RichTrade
import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import co.nilin.opex.api.ports.postgres.model.TradeModel
import co.nilin.opex.api.ports.postgres.util.isWorking
import java.math.BigDecimal
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object Valid {
    private const val PAIR_SYMBOL = "ETH_USDT"
    private const val TIMESTAMP = 1653125840L
    private val CREATE_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP, 0, ZoneOffset.UTC)
    private val UPDATE_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP + 180, 0, ZoneOffset.UTC)
    private val FROM_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP - 600, 0, ZoneOffset.UTC)
    private val TO_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP + 600, 0, ZoneOffset.UTC)

    val PRINCIPAL = Principal { "98c7ca9b-2d9c-46dd-afa8-b0cd2f52a97c" }

    val MAKER_ORDER_MODEL = OrderModel(
        1,
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        PRINCIPAL.name,
        null, // Binance
        PAIR_SYMBOL,
        1, // MatchingEngine ID
        0.01, // Calculated?
        0.01, // Calculated?
        0.0001,
        0.01,
        "1",
        OrderDirection.ASK,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        100000.0,
        0.001,
        100000.0 * 0.001,
        CREATE_DATE,
        UPDATE_DATE
    )

    val TAKER_ORDER_MODEL = OrderModel(
        2,
        "157b9b4a-cc66-43b9-b30b-40a8b66ea6aa",
        PRINCIPAL.name,
        null,
        PAIR_SYMBOL,
        2,
        0.01,
        0.01,
        0.0001,
        0.01,
        "1",
        OrderDirection.BID,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        100000.0,
        0.001,
        100000.0 * 0.01,
        CREATE_DATE,
        UPDATE_DATE
    )

    val MAKER_ORDER_STATUS_MODEL = OrderStatusModel(
        MAKER_ORDER_MODEL.ouid,
        0.0, // Filled amount
        0.0, // --> See accountant
        OrderStatus.FILLED.code,
        OrderStatus.FILLED.orderOfAppearance,
        CREATE_DATE
    )

    val TAKER_ORDER_STATUS_MODEL = OrderStatusModel(
        TAKER_ORDER_MODEL.ouid,
        0.0, // Filled amount
        0.0, // --> See accountant
        OrderStatus.FILLED.code,
        OrderStatus.FILLED.orderOfAppearance,
        CREATE_DATE
    )

    val SYMBOL_MAP_MODEL = SymbolMapModel(
        1,
        PAIR_SYMBOL,
        "binance",
        PAIR_SYMBOL.replace("_", "")
    )

    val TRADE_MODEL = TradeModel(
        1,
        1,
        PAIR_SYMBOL,
        0.001, // Minimum of orders quantities
        100000.0,
        100000.0,
        0.001, // Calculated
        0.001, // Calculated
        "ETH",
        "USDT",
        UPDATE_DATE,
        MAKER_ORDER_MODEL.ouid,
        TAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
        PRINCIPAL.name,
        CREATE_DATE
    )

    val MAKER_QUERY_ORDER_RESPONSE = QueryOrderResponse(
        PAIR_SYMBOL,
        MAKER_ORDER_MODEL.ouid,
        1,
        -1, // Binance
        "", // Binance
        BigDecimal.valueOf(100000.0),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(0.0),
        BigDecimal.valueOf(0.0),
        OrderStatus.FILLED,
        TimeInForce.GTC,
        OrderType.LIMIT,
        OrderSide.SELL,
        null,
        null,
        Date.from(CREATE_DATE.toInstant(ZoneOffset.UTC)),
        Date.from(UPDATE_DATE.toInstant(ZoneOffset.UTC)),
        OrderStatus.FILLED.isWorking(),
        BigDecimal.valueOf(100000.0 * 0.001)
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
        PAIR_SYMBOL,
        MAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
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
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        0
    )

    val RICH_ORDER_UPDATE = RichOrderUpdate(
        MAKER_ORDER_MODEL.ouid,
        BigDecimal.valueOf(1000001),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0.08),
        OrderStatus.PARTIALLY_FILLED
    )

    val RICH_TRADE = RichTrade(
        1,
        PAIR_SYMBOL,
        MAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
        1,
        OrderDirection.ASK,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        "ETH",
        TAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
        2,
        OrderDirection.ASK,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        "USDT",
        BigDecimal.valueOf(0),
        CREATE_DATE
    )

    val ALL_ORDER_REQUEST = AllOrderRequest(
        PAIR_SYMBOL,
        Date.from(FROM_DATE.toInstant(ZoneOffset.UTC)),
        Date.from(TO_DATE.toInstant(ZoneOffset.UTC)),
        500
    )

    val TRADE_REQUEST = TradeRequest(
        PAIR_SYMBOL,
        1,
        Date.from(FROM_DATE.toInstant(ZoneOffset.UTC)),
        Date.from(TO_DATE.toInstant(ZoneOffset.UTC)),
        500
    )

    val MARKET_TRADE_RESPONSE = MarketTradeResponse(
        PAIR_SYMBOL,
        1,
        BigDecimal.valueOf(100000.0),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(100000.0 * 0.001),
        Date.from(CREATE_DATE.toInstant(ZoneOffset.UTC)),
        true,
        MAKER_ORDER_MODEL.direction == OrderDirection.BID
    )

    val QUERY_ORDER_REQUEST = QueryOrderRequest(
        PAIR_SYMBOL,
        1,
        "2"
    )
}
