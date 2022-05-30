package co.nilin.opex.accountant.ports.postgres

import co.nilin.opex.accountant.ports.postgres.dao.PairConfigRepository
import co.nilin.opex.accountant.ports.postgres.dao.PairFeeConfigRepository
import co.nilin.opex.accountant.ports.postgres.impl.PairConfigLoaderImpl
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.utility.error.data.OpexException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class PairConfigLoaderTest {

    private val pairConfigRepository = mockk<PairConfigRepository> {
        every { findAll() } returns Flux.just(DOC.pairConfigModel, DOC.pairConfigModel)
        every { findById(any() as String) } returns Mono.just(DOC.pairConfigModel)
    }
    private val pairFeeConfigRepository = mockk<PairFeeConfigRepository> {
        every { findAll() } returns Flux.just(DOC.pairFeeConfigModel, DOC.pairFeeConfigModel)
        every { findByPairAndDirectionAndUserLevel(any(), any(), any()) } returns Mono.just(DOC.pairFeeConfigModel)
    }
    private val pairConfigLoader = PairConfigLoaderImpl(pairConfigRepository, pairFeeConfigRepository)

    @Test
    fun givenPairConfigs_whenListNotEmpty_resultIsNotEmptyAndValid(): Unit = runBlocking {
        val configs = pairConfigLoader.loadPairConfigs()
        assertThat(configs.size).isEqualTo(2)
        with(configs[1]) {
            assertThat(pair).isEqualTo(DOC.pairConfig.pair)
            assertThat(leftSideWalletSymbol).isEqualTo(DOC.pairConfig.leftSideWalletSymbol)
            assertThat(rightSideWalletSymbol).isEqualTo(DOC.pairConfig.rightSideWalletSymbol)
            assertThat(leftSideFraction).isEqualTo(DOC.pairConfig.leftSideFraction)
            assertThat(rightSideFraction).isEqualTo(DOC.pairConfig.rightSideFraction)
        }
    }

    @Test
    fun givenPairFeeConfigs_whenListNotEmpty_resultIsNotEmptyAndValid(): Unit = runBlocking {
        val configs = pairConfigLoader.loadPairFeeConfigs()
        assertThat(configs.size).isEqualTo(2)
        with(configs[1]) {
            assertThat(pairConfig.pair).isEqualTo(DOC.pairConfig.pair)
            assertThat(userLevel).isEqualTo(DOC.pairFeeConfigModel.userLevel)
            assertThat(direction).isEqualTo(DOC.pairFeeConfigModel.direction)
            assertThat(makerFee).isEqualTo(DOC.pairFeeConfigModel.makerFee)
            assertThat(takerFee).isEqualTo(DOC.pairFeeConfigModel.takerFee)
        }
    }

    @Test
    fun givenPairDirectionUserLevel_whenPairConfigNotFound_throwsException(): Unit = runBlocking {
        every { pairConfigRepository.findById(any() as String) } returns Mono.empty()
        assertThatThrownBy {
            runBlocking { pairConfigLoader.load("BTC_USDT", OrderDirection.BID, "*") }
        }.isInstanceOf(OpexException::class.java)
    }

    @Test
    fun givenPairDirection_whenUserLevelEmpty_loadWithDefaultUserLevel(): Unit = runBlocking {
        val pair = pairConfigLoader.load("BTC_USDT", OrderDirection.BID, "")
        assertThat(pair).isNotNull
        verify(exactly = 1) { pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(any(), any(), eq("*")) }
    }

    @Test
    fun givenPairDirection_whenPairFeeConfigNotFound_throwsException(): Unit = runBlocking {
        every {
            pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(any(), any(), eq("*"))
        } returns Mono.empty()

        assertThatThrownBy {
            runBlocking { pairConfigLoader.load("BTC_USDT", OrderDirection.BID, "") }
        }.isInstanceOf(OpexException::class.java)
    }

    @Test
    fun givenPairDirectionUserLevel_whenPairFeeConfigNotFound_loadWithDefaultUserLevel(): Unit = runBlocking {
        every {
            pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(any(), any(), eq("1"))
        } returns Mono.empty()

        val pair = pairConfigLoader.load("BTC_USDT", OrderDirection.BID, "1")
        assertThat(pair).isNotNull
        verify(exactly = 1) { pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(any(), any(), eq("*")) }
    }

    @Test
    fun givenPairDirectionUserLevel_whenPairFeeConfigNotFoundWithActualAndDefaultUserLevel_throwsException(): Unit =
        runBlocking {
            every {
                pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(any(), any(), eq("1"))
            } returns Mono.empty()

            every {
                pairFeeConfigRepository.findByPairAndDirectionAndUserLevel(any(), any(), eq("*"))
            } returns Mono.empty()

            assertThatThrownBy {
                runBlocking { pairConfigLoader.load("BTC_USDT", OrderDirection.BID, "1") }
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
        with(pairConfigLoader.load("BTC_USDT", OrderDirection.BID)){
            assertThat(pair).isEqualTo(DOC.pairConfigModel.pair)
            assertThat(leftSideWalletSymbol).isEqualTo(DOC.pairConfigModel.leftSideWalletSymbol)
            assertThat(rightSideWalletSymbol).isEqualTo(DOC.pairConfigModel.rightSideWalletSymbol)
            assertThat(rightSideFraction).isEqualTo(DOC.pairConfigModel.rightSideFraction)
            assertThat(leftSideFraction).isEqualTo(DOC.pairConfigModel.leftSideFraction)
        }
    }

}