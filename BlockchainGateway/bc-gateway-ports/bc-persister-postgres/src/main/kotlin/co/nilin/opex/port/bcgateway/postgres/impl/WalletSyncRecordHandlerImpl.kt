package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.spi.WalletSyncRecordHandler
import co.nilin.opex.port.bcgateway.postgres.dao.DepositRepository
import co.nilin.opex.port.bcgateway.postgres.model.DepositModel
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class WalletSyncRecordHandlerImpl(
    private val depositRepository: DepositRepository
) : WalletSyncRecordHandler {
    @Transactional
    override suspend fun saveReadyToSyncTransfers(chainName: String, deposits: List<Deposit>) {
        val depositsDao = deposits.map {
            DepositModel(
                null,
                it.depositor,
                it.depositorMemo,
                it.amount,
                it.chain,
                it.token,
                it.tokenAddress
            )
        }
        depositRepository.saveAll(depositsDao).awaitFirst()
    }

    override suspend fun findReadyToSyncTransfers(count: Long?): List<Deposit> {
        TODO("Not yet implemented")
    }
}