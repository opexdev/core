package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.QuoteCurrency
import co.nilin.opex.wallet.core.spi.QuoteCurrencyManager
import co.nilin.opex.wallet.ports.postgres.dao.QuoteCurrencyRepository
import co.nilin.opex.wallet.ports.postgres.model.QuoteCurrencyModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class QuoteCurrencyManagerImpl(
    private val quoteCurrencyRepository: QuoteCurrencyRepository
) : QuoteCurrencyManager {

    override suspend fun getAll(isActive: Boolean?): List<QuoteCurrency> {
        return quoteCurrencyRepository.findAllByActive(isActive).toList()
    }

    override suspend fun update(currency: String, isActive: Boolean) {
        quoteCurrencyRepository.findByCurrency(currency).awaitFirstOrNull()?.let { quoteCurrency ->
            quoteCurrencyRepository.save(
                QuoteCurrencyModel(
                    quoteCurrency.id,
                    currency,
                    isActive,
                    LocalDateTime.now(),
                )
            )
        }

    }
}