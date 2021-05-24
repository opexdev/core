package co.nilin.mixchange.port.wallet.postgres.impl

import co.nilin.mixchange.port.wallet.postgres.dao.TransactionRepository
import co.nilin.mixchange.port.wallet.postgres.model.TransactionModel
import co.nilin.mixchange.wallet.core.model.Transaction
import co.nilin.mixchange.wallet.core.spi.TransactionManager
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TransactionManagerImpl(val transactionRepository: TransactionRepository) : TransactionManager {
    override suspend fun save(transaction: Transaction): String {
        return transactionRepository.save(
            TransactionModel(
                null,
                transaction.sourceWallet.id()!!,
                transaction.destWallet.id()!!,
                transaction.sourceAmount,
                transaction.destAmount,
                transaction.description,
                transaction.transferRef,
                LocalDateTime.now()
            )
        ).awaitSingle().id.toString()
    }
}