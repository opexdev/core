package co.nilin.opex.matching.engine.core.engine

import co.nilin.opex.matching.engine.core.eventh.CollectingOrderBookEventSink
import co.nilin.opex.matching.engine.core.inout.OrderCancelCommand
import co.nilin.opex.matching.engine.core.inout.OrderCommand
import co.nilin.opex.matching.engine.core.inout.OrderCreateCommand
import co.nilin.opex.matching.engine.core.model.OrderBookDelta
import co.nilin.opex.matching.engine.core.model.PreparedCommandResult
import co.nilin.opex.matching.engine.core.model.PreparedStateTransition

class OrderBookTransitionPreparer(
) {

    suspend fun prepare(
        currentBook: SimpleOrderBook,
        command: OrderCommand
    ): PreparedCommandResult {

        val beforeSnapshot =
            currentBook.snapshot()

        val eventCollector =
            CollectingOrderBookEventSink()

        val workingBook =
            SimpleOrderBook(
                pair = currentBook.pair,
                replayMode = false,
                eventCollector
            )

        workingBook.rebuild(
            beforeSnapshot
        )

        executeCommand(
            book = workingBook,
            command = command
        )

        val afterSnapshot =
            workingBook.snapshot()

        // We will implement this part next.
//        TODO(
//            "Compare before/after and create PreparedCommandResult"
//        )
       // val delta = OrderBookDelta()
        return PreparedCommandResult(
            currentBook.pair,
            commandId = command.ouid,
            eventCollector.events(),
            PreparedStateTransition(
                beforeSnapshot.sequence,
                workingBook.sequence,
                null,
                workingBook
            )
        )
    }

    private suspend fun executeCommand(
        book: SimpleOrderBook,
        command: OrderCommand
    ) {
        when (command) {
            is OrderCreateCommand ->
                book.handleNewOrderCommand(command)

            is OrderCancelCommand ->
                book.handleCancelCommand(command)

            else -> {}
        }
    }
}