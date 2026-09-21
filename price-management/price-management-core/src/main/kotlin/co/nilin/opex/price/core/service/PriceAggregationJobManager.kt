package co.nilin.opex.price.core.service

import co.nilin.opex.price.core.dto.*
import co.nilin.opex.price.core.spi.PairProviderIncludeLoader
import co.nilin.opex.price.core.spi.PriceConfigLoader
import co.nilin.opex.price.core.spi.PriceProxy
import co.nilin.opex.price.core.spi.RateHistoryLoader
import co.nilin.opex.price.core.spi.RateHistoryPersister
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

private val VALID_MARGIN_RANGE = BigDecimal.ZERO..BigDecimal.ONE

class PriceAggregationJobManager(
    private val priceConfigLoader: PriceConfigLoader,
    private val pairProviderIncludeLoader: PairProviderIncludeLoader,
    private val priceProxy: PriceProxy,
    private val rateHistoryLoader: RateHistoryLoader,
    private val rateHistoryPersister: RateHistoryPersister,
    private val rateSyncService: RateSyncService,
    private val maxPriceAge: Duration,
    private val outlierThresholdPercent: BigDecimal
) {

    private val logger = LoggerFactory.getLogger(PriceAggregationJobManager::class.java)

    suspend fun updatePrices() {
        val configs = priceConfigLoader.loadActiveAutoConfigs()
        if (configs.isEmpty()) return

        val symbolPrices = priceProxy.getPrices(configs.map { it.symbol }).associateBy { it.symbol }

        configs.forEach { config ->
            try {
                updateSymbolPrice(config, symbolPrices[config.symbol])
            } catch (e: Exception) {
                logger.error("Failed to update price for symbol=${config.symbol}: ${e.message}", e)
            }
        }
    }

    private suspend fun updateSymbolPrice(config: PairRateConfig, symbolPrices: SymbolPrices?) {
        if (symbolPrices == null || symbolPrices.prices.isEmpty()) {
            logger.warn("No provider prices received for symbol=${config.symbol}")
            return
        }

        val includedProviders = pairProviderIncludeLoader.loadIncludedProviders(config.symbol)
            .map { it.provider }
            .toSet()

        val proxyProviders = symbolPrices.prices.map { it.provider }.toSet()
        val missingProviders = includedProviders - proxyProviders
        if (missingProviders.isNotEmpty()) {
            logger.warn("Symbol=${config.symbol}: selected provider(s) $missingProviders returned no price from the proxy")
        }

        val includedPrices = symbolPrices.prices.filter { it.provider in includedProviders }
        if (includedPrices.isEmpty()) {
            logger.warn("No included providers matched for symbol=${config.symbol}")
            return
        }

        val freshPrices = rejectStale(config.symbol, includedPrices)
        if (freshPrices.isEmpty()) {
            logger.warn("Symbol=${config.symbol}: all included provider prices are stale (older than $maxPriceAge), skipping")
            return
        }

        val prices = rejectOutliers(config.symbol, freshPrices)
        val strategy = config.strategy
        val margin = config.margin
        if (strategy == null || margin == null) {
            logger.warn("AUTO symbol=${config.symbol} is missing strategy/margin, skipping")
            return
        }

        if (margin !in VALID_MARGIN_RANGE) {
            logger.warn("Margin $margin for symbol=${config.symbol} is outside the valid 0..1 range, skipping")
            return
        }

        val finalPrice = strategy.apply(prices) * margin
        val previousPrice = rateHistoryLoader.loadLatest(config.symbol)?.price

        rateHistoryPersister.saveRateHistory(
            RateHistory(
                symbol = config.symbol,
                price = finalPrice,
                createdDate = LocalDateTime.now(),
                source = PriceMode.AUTO
            )
        )
        rateSyncService.syncIfChanged(config.symbol, finalPrice, previousPrice)
        logger.info(
            "Updated price for symbol=${config.symbol} -> price=$finalPrice " +
                    "(strategy=$strategy, providers count=${prices.size}, margin=$margin)"
        )
    }

    private fun rejectStale(symbol: String, prices: List<ProviderPrice>): List<ProviderPrice> {
        val now = LocalDateTime.now()
        val (fresh, stale) = prices.partition { Duration.between(it.timestamp, now) <= maxPriceAge }
        if (stale.isNotEmpty()) {
            logger.warn(
                "Symbol=$symbol: dropping stale price(s) from ${stale.map { it.provider }} " +
                        "(older than $maxPriceAge)"
            )
        }
        return fresh
    }

    private fun rejectOutliers(symbol: String, prices: List<ProviderPrice>): List<BigDecimal> {
        val (accepted, outliers) = PriceOutlierFilter.partition(
            prices.map { it.provider to it.price },
            outlierThresholdPercent
        )
        if (outliers.isNotEmpty()) {
            logger.warn(
                "Symbol=$symbol: dropping outlier price(s) ${outliers.map { "${it.first}=${it.second}" }} " +
                        "(more than $outlierThresholdPercent% off the median)"
            )
        }
        return accepted.map { it.second }
    }
}
