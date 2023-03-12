package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.ports.postgres.dao.FinancialActionRepository
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
        ).map { loadFinancialAction(it.id)!! }
            .toList()
    }

    override suspend fun findLast(userUuid: String, ouid: String): FinancialAction? {
        return financialActionRepository.findByOuidAndUserUuid(
            ouid, userUuid, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"))
        ).map { loadFinancialAction(it.id) }
            .firstOrNull()
    }

    override suspend fun countUnprocessed(userUuid: String, symbol: String, eventType: String): Long {
        return financialActionRepository.findByUuidAndSymbolAndEventTypeAndStatus(
            userUuid,
            symbol,
            eventType,
            FinancialActionStatus.CREATED
        ).awaitFirstOrElse { BigDecimal.ZERO }.toLong()
    }

    private suspend fun loadFinancialAction(id: Long?): FinancialAction? {
        if (id != null) {
            val fim = financialActionRepository.findById(id).awaitFirst()
            return FinancialAction(
                loadFinancialAction(fim.parentId),
                fim.eventType,
                fim.pointer,
                fim.symbol,
                fim.amount,
                fim.sender,
                fim.senderWalletType,
                fim.receiver,
                fim.receiverWalletType,
                fim.createDate,
                fim.status,
                fim.uuid,
                fim.id
            )
        }
        return null
    }
}