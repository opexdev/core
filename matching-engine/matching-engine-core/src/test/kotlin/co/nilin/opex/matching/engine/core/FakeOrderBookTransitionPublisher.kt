package co.nilin.opex.matching.engine.core

import co.nilin.opex.matching.engine.core.inout.InputKafkaMetadata
import co.nilin.opex.matching.engine.core.model.PreparedCommandResult
import co.nilin.opex.matching.engine.core.spi.OrderBookTransitionPublisher

class FakeOrderBookTransitionPublisher : OrderBookTransitionPublisher {
    var published: PreparedCommandResult? = null
    var publishCount = 0
    var failure: RuntimeException? = null

    override suspend fun publish(
        prepared: PreparedCommandResult,
        inputMetadata: InputKafkaMetadata
    ) {
        publishCount++
        failure?.let { throw it }
        published = prepared
    }

}