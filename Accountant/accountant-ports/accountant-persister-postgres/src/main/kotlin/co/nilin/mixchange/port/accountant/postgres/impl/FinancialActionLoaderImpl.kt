package co.nilin.mixchange.port.accountant.postgres.impl

import co.nilin.mixchange.accountant.core.model.FinancialAction
import co.nilin.mixchange.accountant.core.model.FinancialActionStatus
import co.nilin.mixchange.accountant.core.spi.FinancialActionLoader
import co.nilin.mixchange.port.accountant.postgres.dao.FinancialActionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class FinancialActionLoaderImpl(val financialActionRepository: FinancialActionRepository) : FinancialActionLoader {

    override suspend fun loadUnprocessed(offset: Long, size: Long): List<FinancialAction> {
        return financialActionRepository.findByStatus(
            FinancialActionStatus.CREATED.name,
            PageRequest.of(offset.toInt(), size.toInt(), Sort.by(Sort.Direction.ASC, "createDate"))
        ).map { fim ->
            loadFinancialAction(fim.id)!!
        }.toList()
    }

    override suspend fun findLast(uuid: String, ouid: String): FinancialAction? {
        return financialActionRepository.findByOuidAndUuid(
            ouid, uuid, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"))
        ).map { fim ->
            loadFinancialAction(fim.id)
        }.firstOrNull()
    }

    private suspend fun loadFinancialAction(id: Long?): FinancialAction? {
        if (id != null) {
            val fim = financialActionRepository.findById(id).awaitFirst()
            return FinancialAction(
                fim.id,
                loadFinancialAction(fim.parentId),
                fim.eventType,
                fim.pointer,
                fim.symbol,
                BigDecimal.valueOf(fim.amount),
                fim.sender,
                fim.senderWalletType,
                fim.receiver,
                fim.receiverWalletType,
                fim.createDate
            )
        }
        return null
    }

    override suspend fun countUnprocessed(uuid: String, symbol: String, eventType: String): Long {
        return financialActionRepository.findByUuidAndSymbolAndEventTypeAndStatus(uuid, symbol, eventType, FinancialActionStatus.CREATED)
            .awaitFirstOrElse { BigDecimal.ZERO }
            .toLong()
    }
}