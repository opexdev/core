package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.ports.postgres.dao.SymbolMapRepository
import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SymbolMapperTest {
    private val symbolMapRepository: SymbolMapRepository = mock()
    private val symbolMapper = SymbolMapperImpl(symbolMapRepository)

    @BeforeAll
    fun setUp() {
        stubbing(symbolMapRepository) {
            on {
                findByAliasKeyAndAlias("binance", "ETHUSDT")
            } doReturn Mono.just(
                SymbolMapModel(
                    1,
                    "ETH_USDT",
                    "binance",
                    "ETHUSDT"
                )
            )
            on {
                findByAliasKeyAndSymbol("binance", "ETH_USDT")
            } doReturn Mono.just(
                SymbolMapModel(
                    1,
                    "ETH_USDT",
                    "binance",
                    "ETHUSDT"
                )
            )
            on {
                findAll()
            } doReturn Flux.just(
                SymbolMapModel(
                    1,
                    "ETH_USDT",
                    "binance",
                    "ETHUSDT"
                )
            )
        }
    }

    @Test
    fun givenValidSymbol_whenMap_thenReturnAlias(): Unit = runBlocking {
        val alis = symbolMapper.map("ETH_USDT")

        assertThat(alis).isEqualTo("ETHUSDT")
    }

    @Test
    fun givenValidAlias_whenUnmap_thenReturnSymbol(): Unit = runBlocking {
        val symbol = symbolMapper.unmap("ETHUSDT")

        assertThat(symbol).isEqualTo("ETH_USDT")
    }

    @Test
    fun given_whenSymbolToAliasMap_thenReturnSymbol(): Unit = runBlocking {
        val map = symbolMapper.symbolToAliasMap()

        assertThat(map).isNotNull
        assertThat(map.size).isEqualTo(1)
        assertThat(map["ETH_USDT"]).isNotNull()
    }
}
