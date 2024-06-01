package co.nilin.opex.accountant.core.service

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.*
import co.nilin.opex.utility.error.data.OpexException
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClientResponseException

class FinancialActionJobManagerImpl(
    private val financialActionLoader: FinancialActionLoader,
    private val financialActionPersister: FinancialActionPersister,
    private val walletProxy: WalletProxy
) : FinancialActionJobManager {

    private val logger = LoggerFactory.getLogger(FinancialActionJobManagerImpl::class.java)

    override suspend fun processFinancialActions(offset: Long, size: Long) {
        financialActionLoader.loadReadyToProcess(offset, size)
            .also { if (it.isNotEmpty()) logger.info("Processing ${it.size} financial actions") }
            .forEach {
                try {
                    if (it.parent != null) {
                        val reloadParent = financialActionLoader.loadFinancialAction(it.parent.id)!!
                        if (reloadParent.status != FinancialActionStatus.PROCESSED) {
                            logger.warn("Financial job (uuid=${it.uuid}) skipped because of parent status: uuid=${reloadParent.uuid}, status=${reloadParent.status}")
                            return@forEach
                        }
                    }
                    walletProxy.transfer(
                        it.symbol,
                        it.senderWalletType,
                        it.sender,
                        it.receiverWalletType,
                        it.receiver,
                        it.amount,
                        it.eventType + it.pointer,
                        it.id.toString(),
                        it.category.toString(),
                        it.detail
                    )
                    financialActionPersister.updateStatusNewTx(it, FinancialActionStatus.PROCESSED)

                } catch (e: WebClientResponseException) {
                    logger.error("Retry financial job error for ${it.uuid}: ${e.message}")
                    financialActionPersister.updateWithError(
                        it,
                        e.javaClass.name,
                        e.message,
                        e.responseBodyAsString
                    )
                } catch (e: Exception) {
                    logger.error("Retry financial job error for ${it.uuid}: ${e.message}")
                    financialActionPersister.updateWithError(
                        it,
                        if (e is OpexException) e.error.errorName() ?: "" else e.javaClass.name,
                        e.message
                    )
                }
            }
    }

    override suspend fun retryFinancialActions(limit: Int) {
        financialActionLoader.loadRetries(limit)
            .also { if (it.isNotEmpty()) logger.info("Retrying ${it.size} financial actions") }
            .forEach {
                try {
                    walletProxy.transfer(
                        it.symbol,
                        it.senderWalletType,
                        it.sender,
                        it.receiverWalletType,
                        it.receiver,
                        it.amount,
                        it.eventType + it.pointer,
                        it.id.toString(),
                        it.category.toString(),
                        it.detail
                    )
                    with(financialActionPersister) {
                        updateStatusNewTx(it, FinancialActionStatus.PROCESSED)
                        retrySuccessful(it)
                    }
                } catch (e: WebClientResponseException) {
                    logger.error("Retry financial job error for ${it.uuid}: ${e.message}")
                    financialActionPersister.updateWithError(
                        it,
                        e.javaClass.name,
                        e.message,
                        e.responseBodyAsString
                    )
                } catch (e: Exception) {
                    logger.error("Retry financial job error for ${it.uuid}: ${e.message}")
                    financialActionPersister.updateWithError(
                        it,
                        if (e is OpexException) e.error.errorName() ?: "" else e.javaClass.name,
                        e.message
                    )
                }
            }
    }

    private fun extractFAParents(financialAction: FinancialAction, list: ArrayList<FinancialAction>) {
        if (financialAction.parent != null) {
            extractFAParents(financialAction.parent, list)
        }

        if (!list.contains(financialAction))
            list.add(financialAction)
    }
}