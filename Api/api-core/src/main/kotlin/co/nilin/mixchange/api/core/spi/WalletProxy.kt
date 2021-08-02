package co.nilin.mixchange.api.core.spi

import co.nilin.mixchange.api.core.inout.OwnerLimitsResponse
import co.nilin.mixchange.api.core.inout.Wallet

interface WalletProxy {

    suspend fun getWallets(uuid: String?, token: String?): List<Wallet>

    suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse

}