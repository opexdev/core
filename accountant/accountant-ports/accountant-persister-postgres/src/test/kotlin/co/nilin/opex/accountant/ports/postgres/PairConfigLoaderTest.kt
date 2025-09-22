package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.impl.PairConfigLoaderImpl
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.utility.error.data.OpexException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class PairConfigLoaderTest {

    private val pairConfigRepository = mockk<PairConfigRepository> {
        every { findAll() } returns Flux.just(Valid.pairConfigModel, Valid.pairConfigModel)
        every { findById(any() as String) } returns Mono.just(Valid.pairConfigModel)
    }
    private val pairConfigLoader = PairConfigLoaderImpl(pairConfigRepository)

    @Test
    fun givenPairConfigs_whenListNotEmpty_resultIsNotEmptyAndValid(): Unit = runBlocking {
        val configs = pairConfigLoader.loadPairConfigs()
        assertThat(configs.size).isEqualTo(2)
        with(configs[1]) {
            assertThat(pair).isEqualTo(Valid.pairConfig.pair)
            assertThat(leftSideWalletSymbol).isEqualTo(Valid.pairConfig.leftSideWalletSymbol)
            assertThat(rightSideWalletSymbol).isEqualTo(Valid.pairConfig.rightSideWalletSymbol)
            assertThat(leftSideFraction).isEqualTo(Valid.pairConfig.leftSideFraction)
            assertThat(rightSideFraction).isEqualTo(Valid.pairConfig.rightSideFraction)
        }
    }

    @Test
    fun givenPairDirectionUserLevel_whenPairConfigNotFound_throwsException(): Unit = runBlocking {
        every { pairConfigRepository.findById(any() as String) } returns Mono.empty()
        assertThatThrownBy {
            runBlocking { pairConfigLoader.load("BTC_USDT", OrderDirection.BID) }
        }.isInstanceOf(OpexException::class.java)
    }

    @Test
    fun givenPairDirection_whenPairConfigNotFound_throwException(): Unit = runBlocking {
        every { pairConfigRepository.findById(any() as String) } returns Mono.empty()
        assertThatThrownBy {
            runBlocking { pairConfigLoader.load("BTC_USDT", OrderDirection.BID) }
        }.isInstanceOf(OpexException::class.java)
    }

    @Test
    fun givenPairDirection_whenConfigLoaded_returnValidPairConfig(): Unit = runBlocking {
        with(pairConfigLoader.load("BTC_USDT", OrderDirection.BID)) {
            assertThat(pair).isEqualTo(Valid.pairConfigModel.pair)
            assertThat(leftSideWalletSymbol).isEqualTo(Valid.pairConfigModel.leftSideWalletSymbol)
            assertThat(rightSideWalletSymbol).isEqualTo(Valid.pairConfigModel.rightSideWalletSymbol)
            assertThat(rightSideFraction).isEqualTo(Valid.pairConfigModel.rightSideFraction)
            assertThat(leftSideFraction).isEqualTo(Valid.pairConfigModel.leftSideFraction)
        }
    }

}