package co.nilin.opex.price.ports.postgres.utils

import co.nilin.opex.price.core.dto.PairProviderInclude
import co.nilin.opex.price.core.dto.PairRateConfig
import co.nilin.opex.price.core.dto.PriceMode
import co.nilin.opex.price.core.dto.PriceStrategy
import co.nilin.opex.price.core.dto.RateHistory
import co.nilin.opex.price.ports.postgres.model.PairProviderIncludeModel
import co.nilin.opex.price.ports.postgres.model.PairRateConfigModel
import co.nilin.opex.price.ports.postgres.model.RateHistoryModel

fun PairRateConfigModel.asCoreModel() = PairRateConfig(
    symbol = symbol,
    strategy = strategy?.let { PriceStrategy.valueOf(it) },
    margin = margin,
    isActive = isActive,
    priceMode = PriceMode.valueOf(priceMode)
)

fun PairProviderIncludeModel.asCoreModel() = PairProviderInclude(
    symbol = symbol,
    provider = provider
)

fun PairProviderInclude.asModel() = PairProviderIncludeModel(
    symbol = symbol,
    provider = provider
)

fun RateHistoryModel.asCoreModel() = RateHistory(
    symbol = symbol,
    price = price,
    createdDate = createdDate,
    source = PriceMode.valueOf(source)
)

fun RateHistory.asModel() = RateHistoryModel(
    symbol = symbol,
    price = price,
    createdDate = createdDate,
    source = source.name
)
