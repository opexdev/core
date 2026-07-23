package co.nilin.opex.matching.engine.core.engine

import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.FakeOrderBookStore
import co.nilin.opex.matching.engine.core.FakeOrderBookTransitionPublisher
import co.nilin.opex.matching.engine.core.eventh.CollectingOrderBookEventSink
import co.nilin.opex.matching.engine.core.inout.InputKafkaMetadata
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.model.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class OrderCommandProcessorTest {
    private val pair = Pair("BTC", "USDT")
    private val userId = "user-1"
    private lateinit var preparer: OrderBookTransitionPreparer
    private lateinit var publisher: FakeOrderBookTransitionPublisher
    private lateinit var recoveryManager: MatchingEngineRecoveryManager
    private lateinit var originalBook: SimpleOrderBook
    private lateinit var orderBookStore: FakeOrderBookStore
    private lateinit var processor: OrderCommandProcessor

    @BeforeEach
    fun setup() {
        preparer = OrderBookTransitionPreparer()
        recoveryManager = MatchingEngineRecoveryManager()
        originalBook = SimpleOrderBook(pair, false, CollectingOrderBookEventSink())
        orderBookStore = FakeOrderBookStore(originalBook)
        publisher = FakeOrderBookTransitionPublisher()
        processor = OrderCommandProcessor(preparer, publisher, recoveryManager, orderBookStore)
        recoveryManager.markRunning()

    }

    @Test
    fun givenValidCommand_whenPublishedSuccessfully_thenPreparedBookReplacesLiveBook() {
        runBlocking {

            val command = generateBidCommand()

            val inputMetadata = generateMetaDta()

            processor.process(
                command = command,
                currentBook = originalBook,
                kafkaMetaData = inputMetadata
            )

            assertEquals(
                1,
                publisher.publishCount
            )

            assertEquals(
                1,
                orderBookStore.replaceCount
            )

            assertNotSame(
                originalBook,
                orderBookStore.currentBook
            )

            assertSame(
                publisher.published
                    ?.stateTransition
                    ?.preparedBook,
                orderBookStore.currentBook
            )
        }

    }


    @Test
    fun givenValidCommand_whenPublishedThrowsException_thenReplacementWillNeverCall() {
        runBlocking {
            val command = generateBidCommand()

            preparer = mockk()
            processor = OrderCommandProcessor(preparer, publisher, recoveryManager, orderBookStore)
            val inputMetadata = generateMetaDta()

            val prepared = PreparedCommandResult(pair, command.ouid, emptyList(), null)

            coEvery {
                preparer.prepare(
                    originalBook, command
                )
            }.returns(prepared)

            publisher.failure = RuntimeException("Kafka exception in publishment")

            recoveryManager.markRunning()


            assertThrows(RuntimeException::class.java) {
                runBlocking {
                    processor.process(
                        command = command,
                        currentBook = originalBook,
                        kafkaMetaData = inputMetadata
                    )
                }
            }

            assertEquals(
                1,
                publisher.publishCount
            )

            assertEquals(
                0,
                orderBookStore.replaceCount
            )
        }
    }

    @Test
    fun givenPublicationSucceeds_whenLiveBookReplacementThrows_thenProcessorEntersRecovery() {
        val command = generateBidCommand()
        val inputMetadata = generateMetaDta()

        val replacementFailure =
            RuntimeException("Live-book replacement failed")

        orderBookStore.failure =
            replacementFailure

        recoveryManager.markRunning()

        val thrown = assertThrows<RuntimeException> {
            runBlocking {
                processor.process(
                    command = command,
                    currentBook = originalBook,
                    kafkaMetaData = inputMetadata
                )
            }
        }

        // Publication completed before the local replacement.
        assertEquals(
            1,
            publisher.publishCount
        )

        // Replacement was attempted once.
        assertEquals(
            1,
            orderBookStore.replaceCount
        )

        // The local live book was not changed.
        assertSame(
            originalBook,
            orderBookStore.currentBook
        )

        // Processor translated the internal replacement failure
        // into the public temporary-unavailable error.
        assertEquals(
            OpexError.TemporaryInUnavailable.exception()::class,
            thrown::class
        )

        // enterRecovery() means the engine must no longer be RUNNING.
        assertThrows<Exception> {
            recoveryManager.ensureRunning()
        }

        assertThrows<Exception> {
            recoveryManager.ensureRunning()
        }

        assertEquals(MatchingEngineState.RECOVERING, recoveryManager.currentState())
    }


    private fun generateBidCommand() = OrderCreateCommand(
        ouid = UUID.randomUUID().toString(),
        uuid = userId,
        pair = pair,
        price = 100,
        quantity = 5,
        direction = OrderDirection.BID,
        matchConstraint = MatchConstraint.GTC,
        orderType = OrderType.LIMIT_ORDER
    )

    private fun generateCancelCommand(ouid: String) = OrderCancelCommand(
        ouid = ouid,
        uuid = userId,
        pair = pair,
        orderId = Long.MAX_VALUE,
    )

    private fun generateAskCommand() = OrderCreateCommand(
        ouid = UUID.randomUUID().toString(),
        uuid = userId,
        pair = pair,
        price = 100,
        quantity = 2,
        direction = OrderDirection.ASK,
        matchConstraint = MatchConstraint.GTC,
        orderType = OrderType.LIMIT_ORDER
    )

    private fun generateOrderBook(eventCollector: CollectingOrderBookEventSink? = null) = SimpleOrderBook(
        pair = pair,
        replayMode = false,
        eventSink = eventCollector ?: CollectingOrderBookEventSink()
    )

    private fun generateMetaDta() = InputKafkaMetadata(
        topic = "order-requests",
        partition = 0,
        offset = 10,
        consumerGroupId = "matching-engine"
    )
}