package co.nilin.opex.matching.engine.app.config


import co.nilin.opex.matching.engine.app.bl.OrderBooks
import co.nilin.opex.matching.engine.core.engine.MatchingEngineRecoveryManager
import co.nilin.opex.matching.engine.core.engine.OrderBookTransitionPreparer
import co.nilin.opex.matching.engine.core.engine.OrderCommandProcessor
import co.nilin.opex.matching.engine.core.spi.OrderBookStore
import co.nilin.opex.matching.engine.core.spi.OrderBookTransitionPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MatchingEngineCoreConfiguration {

    @Bean
    fun matchingEngineRecoveryManager(): MatchingEngineRecoveryManager =
        MatchingEngineRecoveryManager()

    @Bean
    fun orderBookTransitionPreparer() =
        OrderBookTransitionPreparer()

    @Bean
    fun orderBookStore(): OrderBookStore =
        OrderBooks

    @Bean
    fun orderCommandProcessor(
        transitionPreparer: OrderBookTransitionPreparer,
        transitionPublisher: OrderBookTransitionPublisher,
        recoveryManager: MatchingEngineRecoveryManager,
        orderBookStore: OrderBookStore
    ) = OrderCommandProcessor(
        transitionPreparer = transitionPreparer,
        transitionPublisher = transitionPublisher,
        recoveryManager = recoveryManager,
        orderBookStore = orderBookStore
    )
}