package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.DailyAmount
import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletDataResponse
import co.nilin.opex.wallet.core.inout.WalletTotal
import co.nilin.opex.wallet.core.model.WalletType

interface WalletDataManager {

    suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletData>

    suspend fun findWalletDataByCriteria(
        uuid: String?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletDataResponse>

    suspend fun findSystemWalletsTotal(): List<WalletTotal>

    suspend fun findUserWalletsTotal(): List<WalletTotal>?

    suspend fun getLastDaysBalance(
        userId: String,
        quoteCurrency: String? = null,
        n: Int = 31
    ): List<DailyAmount>
}