package co.nilin.opex.matching.engine.core.engine

import co.nilin.opex.matching.engine.core.eventh.OrderBookEventSink
import co.nilin.opex.matching.engine.core.eventh.events.*
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.inout.RejectReason
import co.nilin.opex.matching.engine.core.inout.RequestedOperation
import co.nilin.opex.matching.engine.core.model.*
import exchange.core2.collections.art.LongAdaptiveRadixTreeMap
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class SimpleOrderBook(
    val pair: Pair,
    var replayMode: Boolean,
    var eventSink: OrderBookEventSink
) : OrderBook {

    private val logger = LoggerFactory.getLogger(SimpleOrderBook::class.java)
    val askOrders = LongAdaptiveRadixTreeMap<Bucket>()
    val bidOrders = LongAdaptiveRadixTreeMap<Bucket>()
    val orders = TreeMap<Long, SimpleOrder>()

    var bestAskOrder: SimpleOrder? = null
    var bestBidOrder: SimpleOrder? = null

    val orderCounter = AtomicLong()
    val tradeCounter = AtomicLong()

    var lastOrder: SimpleOrder? = null

    var sequence: Long = 0

    override suspend fun handleNewOrderCommand(orderCommand: OrderCreateCommand): Order? {
        logNewOrder(orderCommand)
        val order = when (orderCommand.matchConstraint) {
            MatchConstraint.GTC -> {
                if (orderCommand.orderType == OrderType.MARKET_ORDER) {
                    emit(
                        RejectOrderEvent(
                            orderCommand.ouid,
                            orderCommand.uuid,
                            orderCommand.pair,
                            orderCommand.price,
                            orderCommand.quantity,
                            orderCommand.direction,
                            orderCommand.matchConstraint,
                            orderCommand.orderType,
                            RequestedOperation.PLACE_ORDER,
                            RejectReason.ORDER_TYPE_NOT_MATCHED_MATCHC
                        )
                    )

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
                emit(
                    CreateOrderEvent(
                        orderCommand.ouid,
                        orderCommand.uuid,
                        order.id!!,
                        orderCommand.pair,
                        orderCommand.price,
                        orderCommand.quantity,
                        order.remainedQuantity(),
                        orderCommand.direction,
                        orderCommand.matchConstraint,
                        orderCommand.orderType
                    )
                )

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
                emit(
                    CreateOrderEvent(
                        orderCommand.ouid, orderCommand.uuid,
                        order.id!!, orderCommand.pair, orderCommand.price,
                        orderCommand.quantity, order.remainedQuantity(),
                        orderCommand.direction, orderCommand.matchConstraint, orderCommand.orderType
                    )
                )
                // try to match instantly
                val queueOrder = matchIocInstantly(order)
                if (queueOrder.filledQuantity != queueOrder.quantity) {
                    emit(
                        CancelOrderEvent(
                            orderCommand.ouid,
                            orderCommand.uuid,
                            queueOrder.id!!,
                            orderCommand.pair,
                            order.price,
                            order.quantity,
                            order.remainedQuantity(),
                            order.direction,
                            order.matchConstraint,
                            order.orderType
                        )
                    )
                }
                queueOrder
            }

            else -> {
                emit(
                    RejectOrderEvent(
                        orderCommand.ouid,
                        orderCommand.uuid,
                        orderCommand.pair,
                        orderCommand.price,
                        orderCommand.quantity,
                        orderCommand.direction,
                        orderCommand.matchConstraint,
                        orderCommand.orderType,
                        RequestedOperation.PLACE_ORDER,
                        RejectReason.OPERATION_NOT_MATCHED_MATCHC
                    )
                )
                null
            }
        }
        lastOrder = order
        logCurrentState()
        return order
    }

    override suspend fun handleCancelCommand(orderCommand: OrderCancelCommand) {
        logger.info(
            """
            ---- CANCEL ${orderCommand.pair.leftSideName}-${orderCommand.pair.rightSideName} ----
            - order id: ${orderCommand.ouid}
            ********************************************
            
        """.trimIndent()
        )

        val simpleOrder = orders.entries.find { it.value.ouid == orderCommand.ouid }
        val order = simpleOrder?.value
        if (order == null /*check for userid*/) {
            emit(
                RejectOrderEvent(
                    orderCommand.ouid,
                    orderCommand.uuid,
                    orderCommand.orderId,
                    orderCommand.pair,
                    RequestedOperation.CANCEL_ORDER,
                    RejectReason.ORDER_NOT_FOUND
                )
            )
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
        emit(
            CancelOrderEvent(
                orderCommand.ouid, orderCommand.uuid,
                orderCommand.orderId, orderCommand.pair,
                order.price, order.quantity,
                order.remainedQuantity(), order.direction,
                order.matchConstraint, order.orderType
            )
        )

        logCurrentState()
    }

    private fun handleCancelOrder(
        order: SimpleOrder,
        bucketQueue: LongAdaptiveRadixTreeMap<Bucket>,
        bestOrder: SimpleOrder?,
        setBestOrder: (SimpleOrder?) -> Unit
    ) {
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

    private suspend fun matchInstantly(order: SimpleOrder): SimpleOrder {
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

    private suspend fun matchIocInstantly(order: SimpleOrder): SimpleOrder {
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

    private suspend fun matchInstantly(
        order: SimpleOrder,
        makerOrder: SimpleOrder?,
        queue: LongAdaptiveRadixTreeMap<Bucket>,
        isPriceMatched: (makerPrice: Long) -> Boolean,
        setNewMarkerOrder: (SimpleOrder?) -> Unit
    ): SimpleOrder {
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
            emit(
                TradeEvent(
                    tradeCounter.incrementAndGet(),
                    pair,
                    order.ouid,
                    order.uuid,
                    order.id
                        ?: 0,
                    order.direction,
                    order.price,
                    order.remainedQuantity(),
                    currentMaker.ouid,
                    currentMaker.uuid,
                    currentMaker.id!!,
                    currentMaker.direction,
                    currentMaker.price,
                    currentMaker.remainedQuantity(),
                    instantMatchQuantity
                )
            )

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

    private fun putGtcInQueue(
        order: SimpleOrder,
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


    fun rebuild(persistentOrderBook: PersistentOrderBook) {
        persistentOrderBook.orders
            ?.filter { order ->
                order.matchConstraint == MatchConstraint.GTC
            }
            ?.map { order ->
                order.toSimpleOrderBook()
            }?.forEach { order -> putGtcInQueue(order) }

        orderCounter.set(persistentOrderBook.orderCounter)
        tradeCounter.set(persistentOrderBook.tradeCounter)
        sequence = persistentOrderBook.sequence
// Reuse the existing in-memory order instance when available instead of creating a duplicate object.
        lastOrder = persistentOrderBook.lastOrder?.let { lo ->
            orders.getOrDefault(lo.id, lo.toSimpleOrderBook())
        }
    }

    private fun PersistentOrder.toSimpleOrderBook(): SimpleOrder =
        SimpleOrder(
            id,
            ouid,
            uuid,
            price,
            quantity,
            matchConstraint,
            orderType,
            direction,
            filledQuantity,
            null,
            null,
            null
        )


    private fun logNewOrder(orderCommand: OrderCreateCommand) {
        logger.info(
            """
            ++++ NEW ${orderCommand.pair.leftSideName}-${orderCommand.pair.rightSideName} ++++
            - ouid: ${orderCommand.ouid}
            - price: ${orderCommand.price}
            - quantity: ${orderCommand.quantity}
            - direction: ${orderCommand.direction}
            ********************************************
            
        """.trimIndent()
        )
    }

    private fun logCurrentState() {
        logger.info(
            """
            ==== STATE ${pair.leftSideName}-${pair.rightSideName} ====
            - askOrders size: ${askOrders.entriesList().size}
            - bidOrders size: ${bidOrders.entriesList().size}
            - orders size: ${orders.size}
            - bestAskOrder: ${bestAskOrder?.ouid}
            - bestBidOrder: ${bestBidOrder?.ouid}
            - lastOrder: ${lastOrder?.ouid}
            ********************************************
            
        """.trimIndent()
        )
    }

    private suspend fun emit(event: CoreEvent) {
        if (!replayMode) {
            eventSink.emit(event)
        }
    }

    fun snapshot(): PersistentOrderBook {
        val snapshot = PersistentOrderBook(pair)
        snapshot.sequence = sequence
        snapshot.orderCounter = orderCounter.get()
        snapshot.tradeCounter = tradeCounter.get()
        snapshot.lastOrder = lastOrder?.persistent()
        snapshot.orders = orders.values.map { order ->
            order.persistent()
        }
        return snapshot
    }

}