package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import org.mockito.kotlin.mock

internal open class CurrencyServiceTestBase {
    protected var currencyRepository: CurrencyRepository = mock { }
    protected var currencyService: CurrencyServiceImpl = CurrencyServiceImpl(currencyRepository)
}
