package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
interface FinancialActionRepository : ReactiveCrudRepository<FinancialActionModel, Long> {

    @Query("select * from fi_actions fi where pointer = :ouid and :uuid in (fi.sender, fi.receiver)")
    fun findByOuidAndUuid(
        @Param("ouid") ouid: String,
        @Param("uuid") uuid: String,
        paging: Pageable
    ): Flow<FinancialActionModel>

    @Query("select count(1) from fi_actions fi where fi.sender = :uuid and fi.symbol = :symbol and fi.event_type = :eventType and fi.status = :status")
    fun findByUuidAndSymbolAndEventTypeAndStatus(
        @Param("uuid") uuid: String,
        @Param("symbol") symbol: String,
        @Param("eventType") eventType: String,
        @Param("status") financialActionStatus: FinancialActionStatus
    ): Mono<BigDecimal>

    @Query("select * from fi_actions fi where status = :status")
    fun findByStatus(@Param("status") status: String, paging: Pageable): Flow<FinancialActionModel>

    @Query("update fi_actions set status = :status where id = :id")
    fun updateStatus(@Param("id") id: Long, @Param("status") status: FinancialActionStatus)

    @Query("update fi_actions set status = :status, retry_count = retry_count + 1 where id = :id")
    fun updateStatusAndIncreaseRetry(@Param("id") id: Long, @Param("status") status: FinancialActionStatus): Mono<Int>

    @Query("update fi_actions set status = :status where id in (:ids)")
    fun updateStatus(ids: List<Long>, status: FinancialActionStatus): Mono<Int>
}