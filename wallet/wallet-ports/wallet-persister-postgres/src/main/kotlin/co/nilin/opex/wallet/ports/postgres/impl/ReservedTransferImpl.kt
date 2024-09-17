package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import co.nilin.opex.wallet.core.model.otc.ReservedTransfer
import co.nilin.opex.wallet.core.spi.ReservedTransferManager
import co.nilin.opex.wallet.ports.postgres.dao.CurrencyRepositoryV2
import co.nilin.opex.wallet.ports.postgres.dao.ReservedTransferRepository
import co.nilin.opex.wallet.ports.postgres.model.ReservedTransferModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class ReservedTransferImpl(private val reservedTransferRepository: ReservedTransferRepository)
    : ReservedTransferManager {
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

    override suspend fun reserve(request: ReservedTransfer):ReservedTransfer {
        request.apply {
            reserveDate = LocalDateTime.now()
            status = ReservedStatus.Created
            expDate = reservedTransferLifeTime?.let { LocalDateTime.now().plusMinutes(it) } ?: null
        }
        reservedTransferRepository.save(request.toModel())?.awaitSingleOrNull()
        return request
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
            status
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
                this.status
        )
    }

}