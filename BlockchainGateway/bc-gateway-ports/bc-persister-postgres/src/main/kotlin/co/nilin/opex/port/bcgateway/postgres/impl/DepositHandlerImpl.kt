package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.spi.DepositHandler
import co.nilin.opex.port.bcgateway.postgres.dao.DepositRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class DepositHandlerImpl(private val depositRepository: DepositRepository) : DepositHandler {

    override suspend fun findDepositsByHash(hash: List<String>): List<Deposit> {
        return depositRepository.findAllByHash(hash)
            .map {
                Deposit(
                    it.id,
                    it.hash,
                    it.depositor,
                    it.depositorMemo,
                    it.amount,
                    it.chain,
                    it.token,
                    it.tokenAddress
                )
            }
            .toList()
    }
}