package co.nilin.opex.matching.engine.core.engine

import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.inout.InputKafkaMetadata
import co.nilin.opex.matching.engine.core.inout.OrderCommand
import co.nilin.opex.matching.engine.core.model.*
import co.nilin.opex.matching.engine.core.spi.OrderBookStore
import co.nilin.opex.matching.engine.core.spi.OrderBookTransitionPublisher

class OrderCommandProcessor(
    private val transitionPreparer: OrderBookTransitionPreparer,
    private val transitionPublisher: OrderBookTransitionPublisher,
    private val recoveryManager: MatchingEngineRecoveryManager,
    private val orderBookStore: OrderBookStore,

    ) {

    suspend fun process(
        command: OrderCommand,
        currentBook: SimpleOrderBook,
        kafkaMetaData: InputKafkaMetadata
    ) {
        recoveryManager.ensureRunning()

        val pairKey = pairKey(command.pair)

        /*
         * This executes matchInstantly(), putGtcInQueue(),
         * cancellation and matching only on a working copy.
         */
        val prepared =
            transitionPreparer.prepare(
                currentBook = currentBook,
                command = command
            )

        /*
         * One Kafka transaction:
         *
         * - all public events
         * - order-book delta, if state changed
         * - consumed input offset + 1
         */
        transitionPublisher.publish(
            prepared = prepared,
            inputMetadata = kafkaMetaData
        )

        /*
         * Kafka has committed successfully.
         * Now make the prepared state live.
         */
        prepared.stateTransition?.let { transition ->
            try {
                orderBookStore.replace(
                    pairKey = pairKey,
                    expected = currentBook,
                    replacement = transition.preparedBook
                )
            } catch (exception: Throwable) {
                recoveryManager.enterRecovery(
                    cause = exception,
                    pair = command.pair,
                    expectedSequence = transition.nextSequence
                )

                throw OpexError.TemporaryInUnavailable.exception()
            }
        }
    }

    private fun pairKey(pair: Pair): String =
        "${pair.leftSideName}_${pair.rightSideName}"
}


