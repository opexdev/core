package co.nilin.opex.matching.engine.core.spi

import co.nilin.opex.matching.engine.core.inout.InputKafkaMetadata
import co.nilin.opex.matching.engine.core.model.PreparedCommandResult

interface OrderBookTransitionPublisher {

    suspend fun publish(
        prepared: PreparedCommandResult,
        inputMetadata: InputKafkaMetadata
    )
}