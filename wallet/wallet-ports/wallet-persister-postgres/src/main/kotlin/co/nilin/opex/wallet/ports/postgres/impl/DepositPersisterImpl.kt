package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.Deposits
import co.nilin.opex.wallet.core.inout.WithdrawResponse
import co.nilin.opex.wallet.core.model.Withdraw
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
class DepositPersisterImpl(
        private val depositRepository: DepositRepository,
) : DepositPersister {


    override suspend fun persist(deposit: Deposit): Deposit {
        val dm = depositRepository.save(
                deposit.toModel()
        ).awaitFirst()

        return dm.toDto()
    }


    override suspend fun findDepositHistory(
            uuid: String,
            coin: String?,
            startTime: LocalDateTime?,
            endTime: LocalDateTime?,
            limit: Int,
            offset: Int,
            ascendingByTime: Boolean?
    ): Deposits {
        val deposits = if (ascendingByTime == true)
            depositRepository.findDepositHistoryAsc(uuid, coin, startTime, endTime, limit, offset)
        else
            depositRepository.findDepositHistoryDesc(uuid, coin, startTime, endTime, limit, offset)
        return Deposits(deposits.map { it.toDto() }.toList())
    }


}