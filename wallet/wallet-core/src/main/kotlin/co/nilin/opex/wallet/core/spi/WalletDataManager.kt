package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletTotal
import co.nilin.opex.wallet.core.inout.WalletType

interface WalletDataManager {

    suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletData>

    suspend fun findSystemWalletsTotal(): List<WalletTotal>

    suspend fun findUserWalletsTotal(): List<WalletTotal>
}