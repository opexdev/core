package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import co.nilin.opex.wallet.ports.postgres.util.toModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private class CurrencyServiceTest {
    private val currencyRepository: CurrencyRepositoryV2 = mockk()
    private val currencyService: CurrencyServiceImplV2 = CurrencyServiceImplV2(currencyRepository)

    @Test
    fun givenCurrency_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        every { currencyRepository.fetchCurrencies(symbol = VALID.CURRENCY.symbol) } returns Flux.just(VALID.CURRENCY.toModel())

        val c = currencyService.fetchCurrencies(FetchCurrency(symbol =  VALID.CURRENCY.symbol))?.currencies?.firstOrNull()

        assertThat(c).isNotNull
        assertThat(c!!.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(c.name).isEqualTo(VALID.CURRENCY.name)
        assertThat(c.precision).isEqualTo(VALID.CURRENCY.precision)
    }

    @Test
    fun givenNoCurrency_whenGetCurrency_thenThrowException(): Unit = runBlocking {
        every { currencyRepository.fetchCurrencies(symbol = VALID.CURRENCY.symbol) } returns Flux.empty()
            assertThrows(OpexException::class.java) { runBlocking {  currencyService.fetchCurrencies(FetchCurrency(symbol = VALID.CURRENCY.symbol) )}}

    }

    @Test
    fun givenNoCurrency_whenGetCurrencyWithEmptySymbol_thenThrowException(): Unit = runBlocking {
        every { currencyRepository.fetchCurrencies(symbol = "") } returns Flux.empty()
        assertThrows(OpexException::class.java) { runBlocking {  currencyService.fetchCurrencies(FetchCurrency(symbol = "")) }}
    }
}
