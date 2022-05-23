package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class CurrencyServiceTest {
    private val currencyRepository: CurrencyRepository = mock { }
    private val currencyService: CurrencyServiceImpl = CurrencyServiceImpl(currencyRepository)

    @Test
    fun givenCurrency_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        stubbing(currencyRepository) {
            on { findBySymbol(VALID.CURRENCY.symbol) } doReturn Mono.just(VALID.CURRENCY.toModel())
        }

        val c = currencyService.getCurrency(VALID.CURRENCY.symbol)

        assertThat(c).isNotNull
        assertThat(c!!.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(c.name).isEqualTo(VALID.CURRENCY.name)
        assertThat(c.precision).isEqualTo(VALID.CURRENCY.precision)
    }

    @Test
    fun givenNoCurrency_whenGetCurrency_thenReturnNull(): Unit = runBlocking {
        stubbing(currencyRepository) {
            on { findBySymbol(VALID.CURRENCY.symbol) } doReturn Mono.empty()
        }

        val c = currencyService.getCurrency(VALID.CURRENCY.symbol)

        assertThat(c).isNull()
    }

    @Test
    fun givenNoCurrency_whenGetCurrencyWithEmptySymbol_thenReturnNull(): Unit = runBlocking {
        stubbing(currencyRepository) {
            on { findBySymbol("") } doReturn Mono.empty()
        }

        val c = currencyService.getCurrency("")

        assertThat(c).isNull()
    }
}
