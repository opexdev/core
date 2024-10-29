package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.spi.DepositPersister
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TraceDepositService (private val depositPersister: DepositPersister,){
    private val logger = LoggerFactory.getLogger(TraceDepositService::class.java)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    suspend fun saveDepositInNewTransaction(deposit: Deposit) {
        logger.info("Going to save a deposit command .....")
        depositPersister.persist(deposit)  // Saves outside the main transaction context
    }
}