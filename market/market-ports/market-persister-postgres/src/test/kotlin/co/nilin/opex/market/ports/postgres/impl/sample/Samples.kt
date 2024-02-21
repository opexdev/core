package co.nilin.opex.market.ports.postgres.impl.sample

import co.nilin.opex.market.core.event.RichOrder
import co.nilin.opex.market.core.event.RichOrderUpdate
import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.ports.postgres.model.LastPrice
import co.nilin.opex.market.ports.postgres.model.OrderModel
import co.nilin.opex.market.ports.postgres.model.OrderStatusModel
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.util.isWorking
import java.math.BigDecimal
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

object VALID {

    private const val USER_LEVEL_REGISTERED = "registered"
    private const val TIMESTAMP = 1653125840L
    private val CREATE_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP, 0, ZoneOffset.UTC)
    private val UPDATE_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP + 180, 0, ZoneOffset.UTC)
    private val FROM_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP - 600, 0, ZoneOffset.UTC)
    private val TO_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP + 600, 0, ZoneOffset.UTC)

    const val ETH_USDT = "ETH_USDT"

    val PRINCIPAL = Principal { "98c7ca9b-2d9c-46dd-afa8-b0cd2f52a97c" }

    val MAKER_ORDER_MODEL = OrderModel(
        1,
        "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
        PRINCIPAL.name,
        null, // Binance
        ETH_USDT,
        1, // MatchingEngine ID
        BigDecimal.valueOf(0.01), // Calculated?
        BigDecimal.valueOf(0.01), // Calculated?
        BigDecimal.valueOf(0.0001),
        BigDecimal.valueOf(0.01),
        USER_LEVEL_REGISTERED,
        OrderDirection.ASK,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(100).stripTrailingZeros(),
        CREATE_DATE,
        UPDATE_DATE
    )

    val MAKER_ORDER = Order(
        1,
        MAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
        null,
        ETH_USDT,
        1,
        BigDecimal.valueOf(0.01),
        BigDecimal.valueOf(0.01), // Calculated?
        BigDecimal.valueOf(0.0001),
        BigDecimal.valueOf(0.01),
        USER_LEVEL_REGISTERED,
        OrderDirection.ASK,
        MatchConstraint.GTC,
        MatchingOrderType.LIMIT_ORDER,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(100).stripTrailingZeros(),
        BigDecimal.valueOf(0),
        BigDecimal.valueOf(0),
        OrderStatus.FILLED,
        CREATE_DATE,
        UPDATE_DATE
    )

    val TAKER_ORDER_MODEL = MAKER_ORDER_MODEL.copy(2, "157b9b4a-cc66-43b9-b30b-40a8b66ea6aa")

    val MAKER_ORDER_STATUS_MODEL = OrderStatusModel(
        MAKER_ORDER_MODEL.ouid,
        BigDecimal.valueOf(0), // Filled amount
        BigDecimal.valueOf(0), // --> See accountant
        OrderStatus.FILLED.code,
        OrderStatus.FILLED.orderOfAppearance,
        CREATE_DATE
    )

    val TRADE_MODEL = TradeModel(
        1,
        1,
        ETH_USDT,
        "ETH",
        "USDT",
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001), // Minimum of orders quantities
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001), // Calculated
        BigDecimal.valueOf(0.001), // Calculated
        "ETH",
        "USDT",
        CREATE_DATE,
        MAKER_ORDER_MODEL.ouid,
        TAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
        PRINCIPAL.name,
        UPDATE_DATE
    )

    val LAST_PRICE_MODEL = LastPrice("ETH", BigDecimal.valueOf(1000))

    val AGGREGATED_ORDER_PRICE_MODEL = AggregatedOrderPriceModel(
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001)
    )

    val ORDER_BOOK_RESPONSE = OrderBook(
        AGGREGATED_ORDER_PRICE_MODEL.price!!,
        AGGREGATED_ORDER_PRICE_MODEL.quantity!!
    )

    val RICH_ORDER = RichOrder(
        null,
        ETH_USDT,
        MAKER_ORDER_MODEL.ouid,
        PRINCIPAL.name,
        USER_LEVEL_REGISTERED,
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
        ETH_USDT,
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
        BigDecimal.valueOf(0),
        CREATE_DATE
    )

    val ALL_ORDER_REQUEST = AllOrderRequest(
        ETH_USDT,
        Date.from(FROM_DATE.toInstant(ZoneOffset.UTC)),
        Date.from(TO_DATE.toInstant(ZoneOffset.UTC)),
        500
    )

    val TRADE_REQUEST = TradeRequest(
        ETH_USDT,
        1,
        Date.from(FROM_DATE.toInstant(ZoneOffset.UTC)),
        Date.from(TO_DATE.toInstant(ZoneOffset.UTC)),
        500
    )

    val MARKET_TRADE_RESPONSE = MarketTrade(
        ETH_USDT,
        "ETH",
        "USDT",
        1,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(0.001),
        BigDecimal.valueOf(100000 * 0.001).stripTrailingZeros(),
        Date.from(UPDATE_DATE.atZone(ZoneId.systemDefault()).toInstant()),
        true,
        MAKER_ORDER_MODEL.direction == OrderDirection.BID
    )

    val QUERY_ORDER_REQUEST = QueryOrderRequest(
        ETH_USDT,
        1,
        "2"
    )
}
