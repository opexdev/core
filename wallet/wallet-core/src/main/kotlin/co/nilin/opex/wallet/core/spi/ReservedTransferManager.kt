package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.AdminSwapResponse
import co.nilin.opex.wallet.core.inout.SwapResponse
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import java.time.LocalDateTime

interface ReservedTransferManager {

    suspend fun fetchValidReserve(reserveNumber: String): ReservedTransfer?

    suspend fun commitReserve(reserveNumber: String)

    suspend fun reserve(request: ReservedTransfer): ReservedTransfer

    suspend fun findByCriteria(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false,
        status: ReservedStatus?
    ): List<SwapResponse>?


    suspend fun findByCriteriaForAdmin(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false,
        status: ReservedStatus?
    ): List<AdminSwapResponse>

    suspend fun countByCriteria(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: ReservedStatus?
    ): Long


}