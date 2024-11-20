package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import co.nilin.opex.wallet.ports.postgres.util.toModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

private class CurrencyServiceTest {
    private val currencyRepository: CurrencyRepositoryV2 = mockk()
    private val currencyService: CurrencyServiceImplV2 = CurrencyServiceImplV2(currencyRepository)

    @Test
    fun givenCurrency_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        every { currencyRepository.fetchCurrency(symbol = VALID.CURRENCY.symbol) } returns Mono.just(VALID.CURRENCY.toModel())

        val c = currencyService.fetchCurrency(FetchCurrency(symbol = VALID.CURRENCY.symbol))

        assertThat(c).isNotNull
        assertThat(c!!.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(c.name).isEqualTo(VALID.CURRENCY.name)
        assertThat(c.precision).isEqualTo(VALID.CURRENCY.precision)
    }

    //todo check
    //These tests are disabled because fetchCurrency should not throw 404. it is a service provider for uppoer services and upper services should decide base on fetchCurrency result
//    @Test
//    fun givenNoCurrency_whenGetCurrency_thenThrowException(): Unit = runBlocking {
//        every { currencyRepository.fetchCurrency(symbol = VALID.CURRENCY.symbol) } returns Mono.empty()
//            assertThrows(OpexException::class.java) { runBlocking {  currencyService.fetchCurrency(FetchCurrency(symbol = VALID.CURRENCY.symbol) )}}
//
//    }
//
//    @Test
//    fun givenNoCurrency_whenGetCurrencyWithEmptySymbol_thenThrowException(): Unit = runBlocking {
//        every { currencyRepository.fetchCurrency(symbol = "") } returns Mono.empty()
//        assertThrows(OpexException::class.java) { runBlocking {  currencyService.fetchCurrency(FetchCurrency(symbol = "")) }}
//    }
}
