package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.impl.CurrencyServiceImpl
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import reactor.core.publisher.Mono

private class CurrencyServiceTest {
    @Mock
    private var currencyRepository: CurrencyRepository

    private var currencyService: CurrencyServiceImpl

    init {
        MockitoAnnotations.openMocks(this)
        currencyRepository = mock {
            on {
                findBySymbol("ETH")
            } doReturn Mono.just(
                CurrencyModel(
                    "ETH",
                    "Ethereum",
                    0.0001
                )
            )
        }
        currencyService = CurrencyServiceImpl(currencyRepository)
    }

    @Test
    fun givenSymbol_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {
        val c = currencyService.getCurrency("ETH")

        assertThat(c).isNotNull
        assertThat(c!!.getSymbol()).isEqualTo("ETH")
    }
}
