package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

private class CurrencyServiceTest {
    private val currencyRepository: CurrencyRepository = mockk()
    private val currencyService: CurrencyServiceImpl = CurrencyServiceImpl(currencyRepository)

    @Test
    fun givenCurrency_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        every { currencyRepository.findBySymbol(VALID.CURRENCY.symbol) } returns Mono.just(VALID.CURRENCY.toModel())

        val c = currencyService.getCurrency(VALID.CURRENCY.symbol)

        assertThat(c).isNotNull
        assertThat(c!!.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(c.name).isEqualTo(VALID.CURRENCY.name)
        assertThat(c.precision).isEqualTo(VALID.CURRENCY.precision)
    }

    @Test
    fun givenNoCurrency_whenGetCurrency_thenReturnNull(): Unit = runBlocking {
        every { currencyRepository.findBySymbol(VALID.CURRENCY.symbol) } returns Mono.empty()

        val c = currencyService.getCurrency(VALID.CURRENCY.symbol)

        assertThat(c).isNull()
    }

    @Test
    fun givenNoCurrency_whenGetCurrencyWithEmptySymbol_thenReturnNull(): Unit = runBlocking {
        every { currencyRepository.findBySymbol("") } returns Mono.empty()

        val c = currencyService.getCurrency("")

        assertThat(c).isNull()
    }
}
