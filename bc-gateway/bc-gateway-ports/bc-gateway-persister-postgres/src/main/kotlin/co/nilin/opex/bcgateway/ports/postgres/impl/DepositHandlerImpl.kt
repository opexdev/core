package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.spi.DepositHandler
import co.nilin.opex.bcgateway.ports.postgres.dao.DepositRepository
import co.nilin.opex.bcgateway.ports.postgres.model.DepositModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component

@Component
class DepositHandlerImpl(private val depositRepository: DepositRepository) : DepositHandler {
    override suspend fun findDepositsByHash(hash: List<String>): List<Deposit> {
        return depositRepository.findAllByHash(hash).map {
            Deposit(
                it.id, it.hash, it.depositor, it.depositorMemo, it.amount, it.chain, it.token, it.tokenAddress
            )
        }.toList()
    }

    override suspend fun saveAll(deposits: List<Deposit>) {
        depositRepository.saveAll(deposits.map {
            DepositModel(
                null, it.hash, it.depositor, it.depositorMemo, it.amount, it.chain, it.token, it.tokenAddress
            )
        }).collectList().awaitSingle()
    }


}
