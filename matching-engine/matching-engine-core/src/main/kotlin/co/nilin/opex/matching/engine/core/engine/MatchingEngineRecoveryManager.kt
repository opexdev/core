package co.nilin.opex.matching.engine.core.engine


import co.nilin.opex.common.OpexError
import co.nilin.opex.matching.engine.core.model.MatchingEngineState
import co.nilin.opex.matching.engine.core.model.Pair
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

class MatchingEngineRecoveryManager {

    private val logger =
        LoggerFactory.getLogger(MatchingEngineRecoveryManager::class.java)

    private val state =
        AtomicReference(MatchingEngineState.STARTING)

    fun ensureRunning() {
        val currentState = state.get()

        if (currentState != MatchingEngineState.RUNNING) {
            throw OpexError.TemporaryInUnavailable.exception()
        }
    }

    fun markRunning() {
        state.set(MatchingEngineState.RUNNING)

        logger.info("Matching engine state changed to RUNNING")
    }

    fun enterRecovery(
        cause: Throwable,
        pair: Pair,
        expectedSequence: Long
    ) {
        val previousState =
            state.getAndSet(MatchingEngineState.RECOVERING)

        if (previousState == MatchingEngineState.RECOVERING) {
            return
        }

        logger.error(
            "Matching engine entered recovery mode. " +
                    "pair={}_{}, expectedSequence={}",
            pair.leftSideName,
            pair.rightSideName,
            expectedSequence,
            cause
        )
    }

    fun markFailed(cause: Throwable) {
        state.set(MatchingEngineState.FAILED)

        logger.error(
            "Matching engine entered FAILED state",
            cause
        )
    }

    fun currentState(): MatchingEngineState =
        state.get()
}