package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.JsonMapper
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
class FinancialActionLoaderImpl(
    private val financialActionRepository: FinancialActionRepository,
    private val jsonMapper: JsonMapper
) : FinancialActionLoader {

    override suspend fun loadUnprocessed(offset: Long, size: Long): List<FinancialAction> {
        return financialActionRepository.findByStatusNot(
            FinancialActionStatus.PROCESSED.name,
            PageRequest.of(offset.toInt(), size.toInt(), Sort.by(Sort.Direction.ASC, "createDate"))
        ).map { loadFinancialAction(it.id)!! }
            .toList()
    }

    override suspend fun loadReadyToProcess(offset: Long, size: Long): List<FinancialAction> {
        return financialActionRepository.findReadyToProcess(
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
        return financialActionRepository.countByUuidAndSymbolAndEventTypeAndStatusNot(
            userUuid,
            symbol,
            eventType,
            FinancialActionStatus.PROCESSED
        ).awaitFirstOrElse { BigDecimal.ZERO }.toLong()
    }

    override suspend fun loadFinancialAction(id: Long?): FinancialAction? {
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
                fim.category,
                if (fim.detail != null) jsonMapper.toMap(
                    jsonMapper.deserialize(
                        fim.detail,
                        Map::class.java
                    )
                ) else emptyMap(),
                fim.status,
                fim.uuid,
                fim.id
            )
        }
        return null
    }
}