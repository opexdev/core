package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.AdminSwapResponse
import co.nilin.opex.wallet.core.inout.SwapResponse
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import co.nilin.opex.wallet.ports.postgres.dao.ReservedTransferRepository
import co.nilin.opex.wallet.ports.postgres.model.ReservedTransferModel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ReservedTransferImpl(private val reservedTransferRepository: ReservedTransferRepository) :
    ReservedTransferManager {
    @Value("\${app.reserved-transfer.life-time}")
    private var reservedTransferLifeTime: Long? = null
    override suspend fun fetchValidReserve(reserveNumber: String): ReservedTransfer? {
        return reservedTransferRepository.findByReserveNumber(reserveNumber)?.awaitSingleOrNull()
            ?.takeIf {
                it.expDate?.let { it > LocalDateTime.now() } ?: true && it.status == ReservedStatus.Created
            }?.toDto()
    }

    override suspend fun commitReserve(reserveNumber: String) {
        reservedTransferRepository.findByReserveNumber(reserveNumber)?.awaitSingleOrNull()?.let {
            reservedTransferRepository.save(it.apply { status = ReservedStatus.Committed })?.awaitSingleOrNull()
        }
    }

    override suspend fun reserve(request: ReservedTransfer): ReservedTransfer {
        request.apply {
            reserveDate = LocalDateTime.now()
            status = ReservedStatus.Created
            expDate = reservedTransferLifeTime?.let { LocalDateTime.now().plusMinutes(it) } ?: null
        }
        reservedTransferRepository.save(request.toModel())?.awaitSingleOrNull()
        return request
    }

    override suspend fun findByCriteria(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
        status: ReservedStatus?
    ): List<SwapResponse>? {
        return reservedTransferRepository.findByCriteria(
            owner,
            sourceSymbol,
            destSymbol,
            startTime,
            endTime,
            ascendingByTime,
            limit,
            offset,
            status
        )?.toList()?.map { it.asResponse() }
    }

    override suspend fun findByCriteriaForAdmin(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
        status: ReservedStatus?
    ): List<AdminSwapResponse> {
        return reservedTransferRepository.findByCriteriaForAdmin(
            owner,
            sourceSymbol,
            destSymbol,
            startTime,
            endTime,
            ascendingByTime,
            limit,
            offset,
            status
        ).toList()
    }

    override suspend fun countByCriteria(
        owner: String?,
        sourceSymbol: String?,
        destSymbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        status: ReservedStatus?
    ): Long {
        return reservedTransferRepository.countByCriteria(
            owner,
            sourceSymbol,
            destSymbol,
            startTime,
            endTime,
            status
        ).awaitFirstOrElse { 0L }
    }

    fun ReservedTransferModel.asResponse(): SwapResponse {
        return SwapResponse(
            reserveNumber,
            sourceSymbol,
            destSymbol,
            senderUuid,
            sourceAmount,
            reservedDestAmount,
            reserveDate,
            expDate,
            status,
            rate
        )
    }

    fun ReservedTransferModel.toDto(): ReservedTransfer {
        return ReservedTransfer(
            id,
            reserveNumber,
            sourceSymbol,
            destSymbol,
            senderWalletType,
            senderUuid,
            receiverWalletType,
            receiverUuid,
            sourceAmount,
            reservedDestAmount,
            reserveDate,
            expDate,
            status,
            rate
        )
    }

    fun ReservedTransfer.toModel(): ReservedTransferModel {
        return ReservedTransferModel(
            null,
            this.reserveNumber,
            this.sourceSymbol,
            this.destSymbol,
            this.senderWalletType,
            this.senderUuid,
            this.receiverWalletType,
            this.receiverUuid,
            this.sourceAmount,
            this.reservedDestAmount,
            this.reserveDate,
            this.expDate,
            this.status,
            this.rate
        )
    }

}