package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.AccountingTransactionModel
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface AccountingTransactionRepository : ReactiveCrudRepository<AccountingTransactionModel, Long> {
    fun findByOccurredAtBetweenOrderByOccurredAtDesc(
        from: LocalDateTime,
        to: LocalDateTime,
        pageable: Pageable
    ): Flux<AccountingTransactionModel>

    fun findByTxIdAndSource(txId: String, source: String): Mono<AccountingTransactionModel>
    fun findByEventIdAndSource(eventId: String, source: String): Mono<AccountingTransactionModel>

    @Query("""
        WITH latest AS (
            SELECT DISTINCT ON (COALESCE(operation_id, source || ':' || COALESCE(reference_id, tx_id))) *
            FROM opex_accounting_transactions
            WHERE type = 'WITHDRAW'
              AND record_role IS DISTINCT FROM 'INTERNAL_MOVEMENT'
              AND COALESCE(lifecycle_transition_at, completed_at, occurred_at) <= :to
            ORDER BY COALESCE(operation_id, source || ':' || COALESCE(reference_id, tx_id)),
                     COALESCE(lifecycle_transition_at, completed_at, occurred_at) DESC,
                     CASE withdraw_status WHEN 'DONE' THEN 3 WHEN 'REJECTED' THEN 2
                          WHEN 'CANCELED' THEN 2 WHEN 'ACCEPTED' THEN 1 ELSE 0 END DESC,
                     id DESC
        )
        SELECT * FROM latest
        WHERE COALESCE(lifecycle_transition_at, completed_at, occurred_at) >= :from
          AND (withdraw_status IN ('ACCEPTED', 'DONE')
               OR (withdraw_status IS NULL AND record_role = 'WITHDRAW_COMPLETION'))
        ORDER BY COALESCE(lifecycle_transition_at, completed_at, occurred_at) DESC, event_id, tx_id
        LIMIT :limit
    """)
    fun findReportableWithdrawals(from: LocalDateTime, to: LocalDateTime, limit: Int): Flux<AccountingTransactionModel>
}
