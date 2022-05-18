package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.ports.postgres.model.CurrencyModel
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import reactor.core.publisher.Mono

internal open class CurrencyServiceTestBase {
    protected var currencyRepository: CurrencyRepository = mock {
        on {
            findBySymbol("ETH")
        } doReturn Mono.just(
            CurrencyModel("ETH", "Ethereum", 0.0001)
        )
        on {
            findBySymbol("")
        } doReturn Mono.empty()
        on {
            findBySymbol("WRONG")
        } doReturn Mono.empty()
    }

    protected var currencyService: CurrencyServiceImpl = CurrencyServiceImpl(currencyRepository)
}
