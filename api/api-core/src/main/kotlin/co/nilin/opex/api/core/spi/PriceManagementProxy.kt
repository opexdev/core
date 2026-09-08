package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.pricemanagement.PairRateConfigView
import co.nilin.opex.api.core.inout.pricemanagement.ProviderPrice
import co.nilin.opex.api.core.inout.pricemanagement.UpsertPairRateConfigRequest

interface PriceManagementProxy {
    suspend fun getConfigs(token: String): List<PairRateConfigView>
    suspend fun getConfig(token: String, symbol: String): PairRateConfigView
    suspend fun upsertConfig(token: String, request: UpsertPairRateConfigRequest): PairRateConfigView
    suspend fun getProvidersPrice(token: String, symbol: String): List<ProviderPrice>
}
