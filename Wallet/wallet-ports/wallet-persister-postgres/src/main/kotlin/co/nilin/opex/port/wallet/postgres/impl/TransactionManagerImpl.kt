package co.nilin.opex.port.wallet.postgres.impl

import co.nilin.opex.port.wallet.postgres.dao.TransactionRepository
import co.nilin.opex.port.wallet.postgres.model.TransactionModel
import co.nilin.opex.wallet.core.model.Transaction
import co.nilin.opex.wallet.core.spi.TransactionManager
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