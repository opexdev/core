package co.nilin.opex.wallet.app

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private class CurrencyServiceTest : CurrencyServiceTestBase() {
    @Test
    fun givenSymbol_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        val c = currencyService.getCurrency("ETH")

        assertThat(c).isNotNull
        assertThat(c!!.getSymbol()).isEqualTo("ETH")
    }

    @Test
    fun givenWrongSymbol_whenGetCurrency_thenReturnNull(): Unit = runBlocking {
        val c = currencyService.getCurrency("WRONG")

        assertThat(c).isNull()
    }

    @Test
    fun givenEmptySymbol_whenGetCurrency_thenReturnNull(): Unit = runBlocking {
        val c = currencyService.getCurrency("")

        assertThat(c).isNull()
    }
}
