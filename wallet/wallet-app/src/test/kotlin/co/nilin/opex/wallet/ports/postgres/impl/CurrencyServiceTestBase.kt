package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import reactor.core.publisher.Mono

internal open class CurrencyServiceTestBase {
    @Mock
    protected var currencyRepository: CurrencyRepository

    protected var currencyService: CurrencyServiceImpl

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
            on {
                findBySymbol("")
            } doReturn Mono.empty()
            on {
                findBySymbol("WRONG")
            } doReturn Mono.empty()
        }
        currencyService = CurrencyServiceImpl(currencyRepository)
    }
}
