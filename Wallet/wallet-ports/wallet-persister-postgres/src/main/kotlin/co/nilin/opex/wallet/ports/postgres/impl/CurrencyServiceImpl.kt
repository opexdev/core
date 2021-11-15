package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepository
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.spi.CurrencyService
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class CurrencyServiceImpl(val currencyRepository: CurrencyRepository) : CurrencyService {

    override suspend fun getCurrency(symbol: String): Currency? {
        return currencyRepository.findById(symbol).awaitFirstOrNull()
    }
}