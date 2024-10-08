package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import java.time.LocalDateTime

interface ReservedTransferManager {

    suspend fun fetchValidReserve(reserveNumber: String): ReservedTransfer?

    suspend fun commitReserve(reserveNumber: String)

    suspend fun reserve(request: ReservedTransfer): ReservedTransfer

    suspend fun findReserves(
        uuid: String,
        coin: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false,
        status: ReservedStatus?
    ): List<ReservedTransfer>?


}