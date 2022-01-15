package co.nilin.opex.referral.core.spi

import co.nilin.opex.accountant.core.model.PairConfig

interface AccountantProxy {
    suspend fun fetchPairConfigs(): List<PairConfig>
}