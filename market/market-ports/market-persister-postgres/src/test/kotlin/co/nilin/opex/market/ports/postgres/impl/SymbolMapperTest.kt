package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.ports.postgres.dao.SymbolMapRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SymbolMapperTest {
    private val symbolMapRepository: SymbolMapRepository = mockk()
    private val symbolMapper = SymbolMapperImpl(symbolMapRepository)

    @BeforeAll
    fun setUp() {
        every {
            symbolMapRepository.findByAliasKeyAndAlias("binance", "ETHUSDT")
        } returns Mono.just(VALID.SYMBOL_MAP_MODEL)
        every {
            symbolMapRepository.findByAliasKeyAndSymbol("binance", VALID.ETH_USDT)
        } returns Mono.just(VALID.SYMBOL_MAP_MODEL)
        every {
            symbolMapRepository.findAll()
        } returns Flux.just(VALID.SYMBOL_MAP_MODEL)
    }

    @Test
    fun givenSymbolAlias_whenMapSymbol_thenReturnAlias(): Unit = runBlocking {
        val alis = symbolMapper.map(VALID.ETH_USDT)

        assertThat(alis).isEqualTo("ETHUSDT")
    }

    @Test
    fun givenSymbolAlias_whenUnmapAlias_thenReturnSymbol(): Unit = runBlocking {
        val symbol = symbolMapper.unmap("ETHUSDT")

        assertThat(symbol).isEqualTo(VALID.ETH_USDT)
    }

    @Test
    fun givenSymbolAlias_whenSymbolToAliasMap_thenReturnMap(): Unit = runBlocking {
        val map = symbolMapper.symbolToAliasMap()

        assertThat(map).isNotNull
        assertThat(map.size).isEqualTo(1)
        assertThat(map[VALID.ETH_USDT]).isNotNull()
    }
}
