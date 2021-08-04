package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.OwnerLimitsResponse
import co.nilin.opex.api.core.inout.Wallet

interface WalletProxy {

    suspend fun getWallets(uuid: String?, token: String?): List<Wallet>

    suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse

}