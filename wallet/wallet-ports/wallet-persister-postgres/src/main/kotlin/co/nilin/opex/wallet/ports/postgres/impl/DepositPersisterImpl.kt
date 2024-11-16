package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.DepositResponse
import co.nilin.opex.wallet.core.inout.Deposits
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.Withdraw
import co.nilin.opex.wallet.core.model.WithdrawStatus
import co.nilin.opex.wallet.core.spi.DepositPersister
import co.nilin.opex.wallet.core.spi.WithdrawPersister
import co.nilin.opex.wallet.ports.postgres.dao.DepositRepository
import co.nilin.opex.wallet.ports.postgres.util.toDto
import co.nilin.opex.wallet.ports.postgres.util.toModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class DepositPersisterImpl(private val depositRepository: DepositRepository) : DepositPersister {

    override suspend fun persist(deposit: Deposit): Deposit {
        val dm = depositRepository.save(deposit.toModel()).awaitFirst()
        return dm.toDto()
    }

    override suspend fun findDepositHistory(
            uuid: String,
            currency: String?,
            startTime: LocalDateTime?,
            endTime: LocalDateTime?,
            limit: Int?,
            offset: Int?,
            ascendingByTime: Boolean?
    ): List<Deposit> {
        val deposits =
                depositRepository.findDepositHistory(uuid, currency, startTime, endTime, limit, offset, ascendingByTime)
        return deposits.map { it.toDto() }.toList()
    }

    override suspend fun findByCriteria(
            ownerUuid: String?,
            symbol: String?,
            sourceAddress: String?,
            transactionRef: String?,
            startTime: LocalDateTime?,
            endTime: LocalDateTime?,
            status: List<DepositStatus>?,
            offset: Int?,
            size: Int?,
            ascendingByTime: Boolean?
    ): List<Deposit> {
        val deposits = depositRepository.findByCriteria(ownerUuid, symbol, sourceAddress, transactionRef, startTime, endTime, status, ascendingByTime, offset, size)

        return deposits.map { it.toDto() }.toList()
    }


}