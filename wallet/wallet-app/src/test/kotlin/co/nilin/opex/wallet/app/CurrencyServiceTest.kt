package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.impl.CurrencyServiceImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

private class CurrencyServiceTest {
    @Mock
    private lateinit var currencyRepository: CurrencyRepository

    private var currencyService: CurrencyServiceImpl

    init {
        MockitoAnnotations.openMocks(this)
        currencyService = CurrencyServiceImpl(currencyRepository)
    }

    @Test
    fun givenSymbol_whenGetCurrency_thenReturnCurrency(): Unit = runBlocking {}
}
