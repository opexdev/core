package co.nilin.opex.matching.core.engine

import co.nilin.opex.matching.core.eventh.EventDispatcher
import co.nilin.opex.matching.core.eventh.events.*
import co.nilin.opex.matching.core.inout.*
import co.nilin.opex.matching.core.model.*
import co.nilin.opex.matching.core.spi.OrderBookPersister
import exchange.core2.collections.art.LongAdaptiveRadixTreeMap
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class SimpleOrderBook(val pair: Pair, var replayMode: Boolean) : OrderBook {

    private val logger =LoggerFactory.getLogger(SimpleOrderBook::class.java)

    val askOrders = LongAdaptiveRadixTreeMap<Bucket>()
    val bidOrders = LongAdaptiveRadixTreeMap<Bucket>()
    val orders = TreeMap<Long, SimpleOrder>()

    var bestAskOrder: SimpleOrder? = null
    var bestBidOrder: SimpleOrder? = null

    val orderCounter = AtomicLong()
    val tradeCounter = AtomicLong()

    var lastOrder: SimpleOrder? = null

    data class SimpleOrder(var id: Long?, val ouid: String, val uuid: String, val price: Long, val quantity: Long, val matchConstraint: MatchConstraint, val orderType: OrderType, val direction: OrderDirection, var filledQuantity: Long, var worse: SimpleOrder?, var better: SimpleOrder?, var bucket: Bucket?) : Order {
        fun remainedQuantity() = quantity - filledQuantity
        override fun id(): Long? = id
        override fun toString(): String {
            return "SimpleOrder(id=$id, price=$price, quantity=$quantity, matchConstraint=$matchConstraint, orderType=$orderType, filledQuantity=$filledQuantity, worse=${worse?.id}, better=${better?.id}, bucket=${bucket?.totalQuantity})"
        }

        override fun persistent(): PersistentOrder {
           return PersistentOrder(id!!, ouid, uuid, price, quantity, matchConstraint, orderType, direction, filledQuantity)
        }
    }

    data class Bucket(val price: Long, var totalQuantity: Long, var ordersCount: Long, var lastOrder: SimpleOrder)

    override fun handleNewOrderCommand(orderCommand: OrderCreateCommand): Order? {
        logger.info("****************** new order received *******************")
        logger.info("** order id: ${orderCommand.ouid}")
        logger.info("** price: ${orderCommand.price}")
        logger.info("** quantity: ${orderCommand.quantity}")
        logger.info("** direction: ${orderCommand.direction}")
        logger.info("*********************************************************")
        println()

        val order = when (orderCommand.matchConstraint) {
            MatchConstraint.GTC -> {
                if (orderCommand.orderType == OrderType.MARKET_ORDER) {
                    if (!replayMode) {
                        EventDispatcher.emit(RejectOrderEvent(orderCommand.ouid, orderCommand.uuid, orderCommand.pair, orderCommand.price, orderCommand.quantity, orderCommand.direction, orderCommand.matchConstraint, orderCommand.orderType, RequestedOperation.PLACE_ORDER, RejectReason.ORDER_TYPE_NOT_MATCHED_MATCHC))
                    }
                    return null
                }
                val order = SimpleOrder(
                    orderCounter.incrementAndGet(),
                    orderCommand.ouid,
                    orderCommand.uuid,
                    orderCommand.price,
                    orderCommand.quantity,
                    orderCommand.matchConstraint,
                    orderCommand.orderType,
                    orderCommand.direction,
                    0,
                    null,
                    null,
                    null
                )
                if (!replayMode) {
                    EventDispatcher.emit(CreateOrderEvent(orderCommand.ouid, orderCommand.uuid,
                        order.id!!, orderCommand.pair, orderCommand.price, orderCommand.quantity, order.remainedQuantity(), orderCommand.direction, orderCommand.matchConstraint, orderCommand.orderType
                    ))
                }
                // try to match instantly
                val queueOrder = matchInstantly(order)
                // if remained quantity > 0 add to queue
                if (queueOrder.filledQuantity != queueOrder.quantity) {
                    putGtcInQueue(queueOrder)
                }
                queueOrder
            }
            MatchConstraint.IOC -> {
                val order = SimpleOrder(
                    orderCounter.incrementAndGet(),
                    orderCommand.ouid,
                    orderCommand.uuid,
                    orderCommand.price,
                    orderCommand.quantity,
                    orderCommand.matchConstraint,
                    orderCommand.orderType,
                    orderCommand.direction,
                    0,
                    null,
                    null,
                    null
                )
                if (!replayMode) {
                    EventDispatcher.emit(
                        CreateOrderEvent(
                            orderCommand.ouid, orderCommand.uuid,
                            order.id!!, orderCommand.pair, orderCommand.price,
                            orderCommand.quantity, order.remainedQuantity(),
                            orderCommand.direction, orderCommand.matchConstraint, orderCommand.orderType
                        )
                    )
                }
                // try to match instantly
                val queueOrder = matchIocInstantly(order)
                if (!replayMode) {
                    if (queueOrder.filledQuantity != queueOrder.quantity) {
                        EventDispatcher.emit(CancelOrderEvent(orderCommand.ouid, orderCommand.uuid,
                            queueOrder.id!!, orderCommand.pair, order.price, order.quantity, order.remainedQuantity(), order.direction, order.matchConstraint, order.orderType
                        ))
                    }
                }
                queueOrder
            }
            else -> {
                if (!replayMode) {
                    EventDispatcher.emit(RejectOrderEvent(orderCommand.ouid, orderCommand.uuid,orderCommand.pair, orderCommand.price, orderCommand.quantity, orderCommand.direction, orderCommand.matchConstraint, orderCommand.orderType, RequestedOperation.PLACE_ORDER, RejectReason.OPERATION_NOT_MATCHED_MATCHC))
                }
                null
            }
        }
        lastOrder = order
        EventDispatcher.emit(OrderBookPublishedEvent(persistent()))
        logCurrentState()
        return order
    }

    override fun handleCancelCommand(orderCommand: OrderCancelCommand) {
        logger.info("********************* order cancel **********************")
        logger.info("** order id: ${orderCommand.ouid}")
        logger.info("*********************************************************")
        println()

        val simpleOrder = orders.entries.find { it.value.ouid == orderCommand.ouid }
        val order = simpleOrder?.value
        if (order == null /*check for userid*/) {
            if (!replayMode) {
                EventDispatcher.emit(RejectOrderEvent(orderCommand.ouid, orderCommand.uuid,orderCommand.orderId, orderCommand.pair, RequestedOperation.CANCEL_ORDER, RejectReason.ORDER_NOT_FOUND))
            }
            return
        } else {
            orders.remove(simpleOrder.key)
        }

        if (order.direction == OrderDirection.BID) {
            handleCancelOrder(order, bidOrders, bestBidOrder) { newBestOrder: SimpleOrder? ->
                bestBidOrder = newBestOrder
            }
        } else {
            handleCancelOrder(order, askOrders, bestAskOrder) { newBestOrder: SimpleOrder? ->
                bestAskOrder = newBestOrder
            }
        }
        if (!replayMode) {
            EventDispatcher.emit(CancelOrderEvent(orderCommand.ouid, orderCommand.uuid,
                    orderCommand.orderId, orderCommand.pair,
                order.price, order.quantity,
                order.remainedQuantity(), order.direction,
                order.matchConstraint, order.orderType
            ))
        }
        EventDispatcher.emit(OrderBookPublishedEvent(persistent()))
        logCurrentState()
    }

    override fun handleEditCommand(orderCommand: OrderEditCommand): Order? {
        val order = orders.remove(orderCommand.orderId)
        if (order == null /*check for userid*/) {
            if (!replayMode) {
                EventDispatcher.emit(RejectOrderEvent(orderCommand.ouid, orderCommand.uuid,orderCommand.orderId, orderCommand.pair, RequestedOperation.EDIT_ORDER, RejectReason.ORDER_NOT_FOUND))
            }
            return order
        }
        if (order.direction == OrderDirection.BID) {
            handleCancelOrder(order, bidOrders, bestBidOrder) { newBestOrder: SimpleOrder? ->
                bestBidOrder = newBestOrder
            }
        } else {
            handleCancelOrder(order, askOrders, bestAskOrder) { newBestOrder: SimpleOrder? ->
                bestAskOrder = newBestOrder
            }
        }
        val newOrder = SimpleOrder(
            order.id,
            orderCommand.ouid,
            orderCommand.uuid,
            orderCommand.price,
            orderCommand.quantity,
            order.matchConstraint,
            order.orderType,
            order.direction,
            order.filledQuantity,
            null,
            null,
            null
        )

        return when (order.matchConstraint) {
            MatchConstraint.GTC -> {
                if (!replayMode) {
                    EventDispatcher.emit(UpdatedOrderEvent(orderCommand.ouid, orderCommand.uuid,
                        order.id!!, orderCommand.pair, order.price, order.quantity,
                        orderCommand.price, orderCommand.quantity, order.remainedQuantity(),
                        order.direction, order.matchConstraint, order.orderType
                    ))
                }
                // try to match instantly
                val queueOrder = matchInstantly(newOrder)
                //if remained quantity > 0 add to queue
                if (queueOrder.filledQuantity != queueOrder.quantity) {
                    putGtcInQueue(queueOrder)
                }
                EventDispatcher.emit(OrderBookPublishedEvent(persistent()))
                queueOrder
            }
            MatchConstraint.IOC -> {
                if (!replayMode) {
                    EventDispatcher.emit(UpdatedOrderEvent(orderCommand.ouid, orderCommand.uuid,
                        order.id!!, orderCommand.pair, order.price, order.quantity, orderCommand.price, orderCommand.quantity, order.remainedQuantity(), order.direction, order.matchConstraint, order.orderType
                    ))
                }
                // try to match instantly
                val queueOrder = matchIocInstantly(newOrder)
                if (!replayMode) {
                    if (queueOrder.filledQuantity != queueOrder.quantity) {
                        EventDispatcher.emit(CancelOrderEvent(orderCommand.ouid, orderCommand.uuid,
                            queueOrder.id!!, orderCommand.pair, order.price, order.quantity, order.remainedQuantity(), order.direction, order.matchConstraint, order.orderType
                        ))
                    }
                }
                EventDispatcher.emit(OrderBookPublishedEvent(persistent()))
                queueOrder
            }
            else -> {
                if (!replayMode) {
                    EventDispatcher.emit(RejectOrderEvent(orderCommand.ouid, orderCommand.uuid,orderCommand.orderId, orderCommand.pair, orderCommand.price, orderCommand.quantity, order.direction, order.matchConstraint, order.orderType, RequestedOperation.EDIT_ORDER, RejectReason.OPERATION_NOT_MATCHED_MATCHC))
                }
                null
            }
        }

    }

    private fun handleCancelOrder(order: SimpleOrder, bucketQueue: LongAdaptiveRadixTreeMap<Bucket>, bestOrder: SimpleOrder?, setBestOrder: (SimpleOrder?) -> Unit) {
        val bucket = order.bucket!!
        bucket.ordersCount--
        bucket.totalQuantity -= order.remainedQuantity()
        if (bucket.lastOrder == order) {
            if (bucket.lastOrder.better == null || bucket.lastOrder.better!!.bucket != bucket) {
                bucketQueue.remove(bucket.price)
            } else
                bucket.lastOrder = bucket.lastOrder.better!!
        }
        order.better?.worse = order.worse
        order.worse?.better = order.better
        if (order == bestOrder)
            setBestOrder(bestOrder.worse)
    }

    private fun matchInstantly(order: SimpleOrder): SimpleOrder {
        if (order.direction == OrderDirection.BID) {
            return matchInstantly(order, bestAskOrder, askOrders, { makerPrice: Long ->
                makerPrice <= order.price
            }) { newMakerOrder: SimpleOrder? ->
                bestAskOrder = newMakerOrder
            }
        } else {
            return matchInstantly(order, bestBidOrder, bidOrders, { makerPrice: Long ->
                makerPrice >= order.price
            }) { newMakerOrder: SimpleOrder? ->
                bestBidOrder = newMakerOrder
            }
        }
    }

    private fun matchIocInstantly(order: SimpleOrder): SimpleOrder {
        if (order.direction == OrderDirection.BID) {
            return matchInstantly(order, bestAskOrder, askOrders, { makerPrice: Long ->
                order.orderType == OrderType.MARKET_ORDER || makerPrice <= order.price

            }) { newMakerOrder: SimpleOrder? ->
                bestAskOrder = newMakerOrder
            }
        } else {
            return matchInstantly(order, bestBidOrder, bidOrders, { makerPrice: Long ->
                order.orderType == OrderType.MARKET_ORDER || makerPrice >= order.price
            }) { newMakerOrder: SimpleOrder? ->
                bestBidOrder = newMakerOrder
            }
        }
    }

    private fun putGtcInQueue(order: SimpleOrder): SimpleOrder {
        if (order.direction == OrderDirection.BID) {
            return putGtcInQueue(order, bidOrders, bestBidOrder, { price, queue ->
                queue.getHigherValue(price)
            }) { newMakerOrder: SimpleOrder? ->
                bestBidOrder = newMakerOrder
            }
        } else {
            return putGtcInQueue(order, askOrders, bestAskOrder, { price, queue ->
                queue.getLowerValue(price)
            }) { newMakerOrder: SimpleOrder? ->
                bestAskOrder = newMakerOrder
            }
        }
    }

    private fun matchInstantly(order: SimpleOrder, makerOrder: SimpleOrder?, queue: LongAdaptiveRadixTreeMap<Bucket>, isPriceMatched: (makerPrice: Long) -> Boolean, setNewMarkerOrder: (SimpleOrder?) -> Unit): SimpleOrder {
        //the best sell price is higher the requested buy price, so no instant match
        if (makerOrder == null || !isPriceMatched(makerOrder.price)) {
            return order
        }
        var currentMaker = makerOrder
        var lastOrderOfMakerBucket = makerOrder.bucket!!.lastOrder
        do {
            val instantMatchQuantity = Math.min(order.remainedQuantity(), currentMaker!!.remainedQuantity())
            order.filledQuantity += instantMatchQuantity
            currentMaker.filledQuantity += instantMatchQuantity
            currentMaker.bucket!!.totalQuantity -= instantMatchQuantity
            if (!replayMode) {
                EventDispatcher.emit(TradeEvent(tradeCounter.incrementAndGet(), pair, order.ouid, order.uuid, order.id
                        ?: 0, order.direction, order.price, order.remainedQuantity(), currentMaker.ouid, currentMaker.uuid, currentMaker.id!!, currentMaker.direction, currentMaker.price, currentMaker.remainedQuantity(), instantMatchQuantity))
            }
            if (currentMaker.remainedQuantity() == 0L) {
                currentMaker.bucket!!.ordersCount--
            }
            //create trade with instantMatchQuantity
            if (currentMaker.remainedQuantity() > 0) {
                break
            }
            //remove the makerOrder
            orders.remove(currentMaker.id!!)
            if (currentMaker == lastOrderOfMakerBucket) {
                queue.remove(currentMaker.price)
                if (currentMaker.worse != null)
                    lastOrderOfMakerBucket = currentMaker.worse!!.bucket!!.lastOrder
            }

            currentMaker = currentMaker.worse
        } while (order.remainedQuantity() > 0
                && currentMaker != null
                && isPriceMatched(currentMaker.price)
        )
        if (currentMaker != null) {
            currentMaker.better = null
        }
        setNewMarkerOrder(currentMaker)
        return order

    }


    private fun putGtcInQueue(order: SimpleOrder,
                      queue: LongAdaptiveRadixTreeMap<Bucket>,
                      bestOrder: SimpleOrder?,
                      betterBucketSelector: (price: Long, queue: LongAdaptiveRadixTreeMap<Bucket>) -> Bucket?,
                      setNewMarkerOrder: (SimpleOrder?) -> Unit
    ): SimpleOrder {
        if (order.id == null)
            order.id = orderCounter.incrementAndGet()
        orders[order.id!!] = order
        var bucket = queue[order.price]
        if (bucket != null) {
            bucket.ordersCount++
            bucket.totalQuantity += order.remainedQuantity()
            order.bucket = bucket
            val bucketLastOrder = bucket.lastOrder
            val worseOfBucketLastOrder = bucketLastOrder.worse
            bucket.lastOrder = order
            bucketLastOrder.worse = order
            if (worseOfBucketLastOrder != null) {
                worseOfBucketLastOrder.better = order
            }
            order.better = bucketLastOrder
            order.worse = worseOfBucketLastOrder
        } else {
            bucket = Bucket(
                order.price,
                order.remainedQuantity(),
                1,
                order
            )
            order.bucket = bucket
            queue.put(order.price, bucket)
            val betterBucket = betterBucketSelector(order.price, queue)
            if (betterBucket != null) {
                val aboveBucketLastOrder = betterBucket.lastOrder
                val worseOrder = aboveBucketLastOrder.worse
                aboveBucketLastOrder.worse = order
                if (worseOrder != null) {
                    worseOrder.better = order
                }
                order.better = aboveBucketLastOrder
                order.worse = worseOrder
            } else {
                if (bestOrder != null)
                    bestOrder.better = order
                order.worse = bestOrder
                setNewMarkerOrder(order)
            }
        }
        return order
    }

    override fun pair(): Pair {
        return pair
    }

    override fun startReplayMode() {
        replayMode = true
    }

    override fun stopReplayMode() {
        replayMode = false
    }

    override fun lastOrder(): Order? {
        return lastOrder
    }

    private fun persistent(): PersistentOrderBook {
        val persistent = PersistentOrderBook(pair)
        persistent.lastOrder = lastOrder?.persistent()
        persistent.orders = orders.values
                .map { order -> order.persistent() }
        return persistent
    }

    fun rebuild(persistentOrderBook: PersistentOrderBook) {
        persistentOrderBook.orders?.map { order ->
            SimpleOrder(
                order.id,
                order.ouid,
                order.uuid,
                order.price,
                order.quantity,
                order.matchConstraint,
                order.orderType,
                order.direction,
                order.filledQuantity,
                null,
                null,
                null
            )
        }?.filter { order -> order.matchConstraint == MatchConstraint.GTC
        }?.forEach { order -> putGtcInQueue(order) }
        orderCounter.set(persistentOrderBook.lastOrder?.id?:0)
    }

    private fun logCurrentState(){
        logger.info("******************** ${pair.leftSideName}-${pair.rightSideName} ********************")
        logger.info("** askOrders size: ${askOrders.entriesList().size}")
        logger.info("** bidOrders size: ${bidOrders.entriesList().size}")
        logger.info("** orders size: ${orders.size}")
        logger.info("** bestAskOrder: ${bestAskOrder?.ouid}")
        logger.info("** bestBidOrder: ${bestBidOrder?.ouid}")
        logger.info("** lastOrder: ${lastOrder?.ouid}")
        logger.info("*********************************************************")
        println()
    }
}