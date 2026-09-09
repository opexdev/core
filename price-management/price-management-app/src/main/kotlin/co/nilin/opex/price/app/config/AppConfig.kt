package co.nilin.opex.price.app.config

import co.nilin.opex.price.core.service.PairRateConfigAdminManager
import co.nilin.opex.price.core.service.PriceAggregationJobManager
import co.nilin.opex.price.core.service.PriceSyncMonitor
import co.nilin.opex.price.core.service.RateSyncService
import co.nilin.opex.price.core.spi.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.Duration

@Configuration
class AppConfig {

    @Bean
    fun rateSyncService(walletRateProxy: WalletRateProxy, notifier: Notifier): RateSyncService {
        return RateSyncService(walletRateProxy, notifier)
    }

    @Bean
    fun priceSyncMonitor(
        priceConfigLoader: PriceConfigLoader,
        rateHistoryLoader: RateHistoryLoader,
        notifier: Notifier,
        @Value("\${app.price-management.stale-alert-age-seconds:120}") staleAlertAgeSeconds: Long,
    ): PriceSyncMonitor {
        return PriceSyncMonitor(
            priceConfigLoader,
            rateHistoryLoader,
            notifier,
            Duration.ofSeconds(staleAlertAgeSeconds)
        )
    }

    @Bean
    fun priceAggregationJobManager(
        priceConfigLoader: PriceConfigLoader,
        pairProviderIncludeLoader: PairProviderIncludeLoader,
        priceProxy: PriceProxy,
        rateHistoryLoader: RateHistoryLoader,
        rateHistoryPersister: RateHistoryPersister,
        rateSyncService: RateSyncService,
        @Value("\${app.price-management.max-price-age-seconds}") maxPriceAgeSeconds: Long,
        @Value("\${app.price-management.outlier-threshold-percent}") outlierThresholdPercent: String
    ): PriceAggregationJobManager {
        return PriceAggregationJobManager(
            priceConfigLoader,
            pairProviderIncludeLoader,
            priceProxy,
            rateHistoryLoader,
            rateHistoryPersister,
            rateSyncService,
            Duration.ofSeconds(maxPriceAgeSeconds),
            BigDecimal(outlierThresholdPercent)
        )
    }

    @Bean
    fun pairRateConfigAdminManager(
        priceConfigLoader: PriceConfigLoader,
        priceConfigPersister: PriceConfigPersister,
        pairProviderIncludeLoader: PairProviderIncludeLoader,
        pairProviderIncludePersister: PairProviderIncludePersister,
        rateHistoryLoader: RateHistoryLoader,
        rateHistoryPersister: RateHistoryPersister,
        rateSyncService: RateSyncService,
        priceProxy: PriceProxy,
    ): PairRateConfigAdminManager {
        return PairRateConfigAdminManager(
            priceConfigLoader,
            priceConfigPersister,
            pairProviderIncludeLoader,
            pairProviderIncludePersister,
            rateHistoryLoader,
            rateHistoryPersister,
            rateSyncService,
            priceProxy
        )
    }
}
