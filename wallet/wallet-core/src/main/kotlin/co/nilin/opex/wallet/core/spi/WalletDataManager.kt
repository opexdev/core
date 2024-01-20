package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.WalletData
import co.nilin.opex.wallet.core.inout.WalletType

interface WalletDataManager {

    suspend fun findWalletDataByCriteria(
        uuid: String?,
        walletType: WalletType?,
        currency: String?,
        limit: Int,
        offset: Int
    ): List<WalletData>
}