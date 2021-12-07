package co.nilin.opex.referral.core.spi

import co.nilin.opex.accountant.core.inout.RichTrade

interface CommissionRewardPersister {
    suspend fun save(trade: RichTrade)
}