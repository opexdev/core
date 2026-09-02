package co.nilin.opex.price.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.price.core.dto.*
import co.nilin.opex.price.core.spi.*
import java.math.BigDecimal
import java.time.LocalDateTime

private val VALID_MARGIN_RANGE = BigDecimal.ZERO..BigDecimal.ONE

/**
 * Admin-facing use cases for managing pair_rate_config: registering a symbol together with its
 * selected providers (and, for MANUAL symbols, its price) in one call.
 */
class PairRateConfigAdminManager(
    private val priceConfigLoader: PriceConfigLoader,
    private val priceConfigPersister: PriceConfigPersister,
    private val pairProviderIncludeLoader: PairProviderIncludeLoader,
    private val pairProviderIncludePersister: PairProviderIncludePersister,
    private val rateHistoryLoader: RateHistoryLoader,
    private val rateHistoryPersister: RateHistoryPersister,
    private val rateSyncService: RateSyncService,
    private val priceProxy: PriceProxy,
) {

    suspend fun getProvidersPrice(symbol: String): List<ProviderPrice> {
        return priceProxy.getProvidersPrice(symbol)
    }

    suspend fun upsertConfig(request: UpsertPairRateConfigRequest): PairRateConfigView {
        validate(request)
        val providers = request.providers.orEmpty()

        priceConfigPersister.savePairRateConfig(
            PairRateConfig(
                symbol = request.symbol,
                strategy = request.strategy,
                margin = request.margin,
                isActive = request.isActive,
                priceMode = request.priceMode
            )
        )
        pairProviderIncludePersister.replaceIncludedProviders(request.symbol, providers)

        if (request.priceMode == PriceMode.MANUAL && request.price != null) {
            recordManualPrice(request.symbol, request.price)
        }

        return PairRateConfigView(
            symbol = request.symbol,
            strategy = request.strategy,
            margin = request.margin,
            isActive = request.isActive,
            priceMode = request.priceMode,
            providers = providers
        )
    }

    private fun validate(request: UpsertPairRateConfigRequest) {
        when (request.priceMode) {
            PriceMode.AUTO -> {
                // strategy required
                if (request.strategy == null) throw OpexError.StrategyRequired.exception()
                // margin required, and within 0..1
                if (request.margin == null || request.margin !in VALID_MARGIN_RANGE) {
                    throw OpexError.InvalidMargin.exception()
                }
                // price only makes sense for MANUAL
                if (request.price != null) throw OpexError.PriceNotAllowedForAutoMode.exception()
                // at least one provider must be selected
                if (request.providers.isNullOrEmpty()) throw OpexError.ProvidersRequired.exception()
            }

            PriceMode.MANUAL -> {
                // strategy/margin are AUTO-only concepts
                if (request.strategy != null) throw OpexError.StrategyNotAllowedForManualMode.exception()
                if (request.margin != null) throw OpexError.MarginNotAllowedForManualMode.exception()
            }
        }
    }

    private suspend fun recordManualPrice(symbol: String, price: BigDecimal) {
        val previousPrice = rateHistoryLoader.loadLatest(symbol)?.price
        rateHistoryPersister.saveRateHistory(
            RateHistory(
                symbol = symbol,
                price = price,
                createdDate = LocalDateTime.now(),
                source = PriceMode.MANUAL
            )
        )
        rateSyncService.syncIfChanged(symbol, price, previousPrice)
    }

    suspend fun getConfig(symbol: String): PairRateConfigView? {
        val config = priceConfigLoader.loadPairRateConfig(symbol) ?: return null
        return config.toView()
    }

    suspend fun getConfigs(): List<PairRateConfigView> {
        return priceConfigLoader.loadPairRateConfigs().map { it.toView() }
    }

    private suspend fun PairRateConfig.toView(): PairRateConfigView {
        val providers = pairProviderIncludeLoader.loadIncludedProviders(symbol).map { it.provider }
        return PairRateConfigView(
            symbol = symbol,
            strategy = strategy,
            margin = margin,
            isActive = isActive,
            priceMode = priceMode,
            providers = providers
        )
    }
}
