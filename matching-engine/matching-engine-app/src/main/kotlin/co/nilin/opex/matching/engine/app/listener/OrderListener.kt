package co.nilin.opex.matching.engine.app.listener

import co.nilin.opex.matching.engine.app.bl.OrderBooks
import co.nilin.opex.matching.engine.core.engine.OrderCommandProcessor
import co.nilin.opex.matching.engine.core.inout.InputKafkaMetadata
import co.nilin.opex.matching.engine.core.inout.OrderRequestEvent
import co.nilin.opex.matching.engine.core.model.Pair
import co.nilin.opex.matching.engine.core.util.toCommand
import co.nilin.opex.matching.engine.ports.kafka.listener.spi.OrderRequestEventListener
import org.slf4j.LoggerFactory

class OrderListener() : OrderRequestEventListener {

    lateinit var orderCommandProcessor: OrderCommandProcessor
    private val logger = LoggerFactory.getLogger(OrderListener::class.java)

    override fun id(): String {
        return "OrderListener"
    }

    override suspend fun onOrder(
        order: OrderRequestEvent,
        partition: Int,
        offset: Long,
        timestamp: Long,
        topic: String,
        consumerGroupId: String
    ) {

        logger.info(
            "OrderRequestEvent received: type={}, ouid={}, partition={}, offset={}",
            order::class.java.simpleName,
            order.ouid,
            partition,
            offset
        )

        val command = order.toCommand()
        val currentBook = OrderBooks.lookupOrderBook(pairKey(order.pair))
        orderCommandProcessor.process(
            command = command,
            currentBook,
            InputKafkaMetadata(topic, partition, offset, consumerGroupId)
        )

    }


    private fun pairKey(pair: Pair): String =
        "${pair.leftSideName}_${pair.rightSideName}"
}