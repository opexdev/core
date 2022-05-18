package co.nilin.opex.wallet.ports.postgres.impl

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private class CurrencyServiceTest : CurrencyServiceTestBase() {
    @Test
    fun givenSymbol_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        val c = currencyService.getCurrency("ETH")

        assertThat(c).isNotNull
        assertThat(c!!.getSymbol()).isEqualTo("ETH")
        assertThat(c.getName()).isEqualTo("Ethereum")
        assertThat(c.getPrecision()).isEqualTo(0.0001)
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
