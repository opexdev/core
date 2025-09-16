package co.nilin.opex.wallet.ports.postgres.service

import co.nilin.opex.wallet.ports.postgres.dao.QuoteCurrencyRepository
import co.nilin.opex.wallet.ports.postgres.model.QuoteCurrencyModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
@Profile("!otc")
class QuoteCurrencyInitializer(
    private val quoteCurrencyRepository: QuoteCurrencyRepository,
    @Value("\${app.symbols}")
    private val symbols: String
) {

    private val logger = LoggerFactory.getLogger(QuoteCurrencyInitializer::class.java)
    val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun initialize() {
        logger.info(
            """
================================================================================================
                                Initialize Quote Currencies
================================================================================================
        """
        )
        scope.launch {
            try {

                val quoteSymbols = symbols.split(",")
                    .mapNotNull { symbol ->
                        val quote = symbol.substringAfter('_', "")
                        if (quote.isNotBlank()) quote else null
                    }
                    .toSet()

                quoteSymbols.forEach { quote ->
                    val existing = quoteCurrencyRepository.findByCurrency(quote).awaitFirstOrNull()
                    if (existing == null) {
                        quoteCurrencyRepository.save(
                            QuoteCurrencyModel(null, quote, false, LocalDateTime.now(),null)
                        ).awaitFirstOrNull()
                        logger.info("Quote currency inserted: $quote")
                    }
                }
                logger.info(
                    """
================================================================================================
                                 Completed Successfully
================================================================================================
            """
                )
            } catch (e: Exception) {
                logger.error("Error initializing Quote Currencies: ${e.message}")
                throw e
            }
        }
    }
}
