package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.otc.ReservedTransfer

interface ReservedTransferManager {

    suspend fun fetchValidReserve(reserveNumber:String): ReservedTransfer?

    suspend fun commitReserve(reserveNumber:String)

    suspend fun reserve(request: ReservedTransfer):ReservedTransfer

}