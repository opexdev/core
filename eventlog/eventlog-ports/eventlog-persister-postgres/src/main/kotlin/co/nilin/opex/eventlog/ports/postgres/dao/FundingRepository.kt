package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.FundingModel
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

interface FundingRepository : ReactiveCrudRepository<FundingModel, Long> {
    fun findByTypeAndEventId(type: String, eventId: String): Mono<FundingModel>
    fun findByTypeAndOperationIdOrderByOccurredAtAsc(type: String, operationId: String): Flux<FundingModel>
    fun findBySourceTopicAndSourcePartitionAndSourceOffset(topic: String, partition: Int, offset: Long): Mono<FundingModel>
    fun findByOccurredAtBetweenOrderByOccurredAtDesc(from: LocalDateTime, to: LocalDateTime, page: Pageable): Flux<FundingModel>

    @Query("""
        WITH ranked AS (
            SELECT f.*, ROW_NUMBER() OVER (
                PARTITION BY type, operation_id
                ORDER BY occurred_at DESC,
                    CASE status WHEN 'DONE' THEN 5 WHEN 'REJECTED' THEN 4 WHEN 'CANCELED' THEN 4
                        WHEN 'ACCEPTED' THEN 3 WHEN 'CREATED' THEN 2 WHEN 'REQUESTED' THEN 1 ELSE 0 END DESC,
                    event_id DESC
            ) AS rn
            FROM opex_funding_operations f
            WHERE occurred_at <= :to
        )
        SELECT * FROM ranked
        WHERE rn = 1 AND occurred_at >= :from
            AND ((type = 'DEPOSIT' AND status = 'DONE')
                OR (type = 'WITHDRAW' AND status IN ('ACCEPTED', 'DONE')))
        ORDER BY occurred_at DESC, event_id DESC
        LIMIT :limit
    """)
    fun findReportable(@Param("from") from: LocalDateTime, @Param("to") to: LocalDateTime, @Param("limit") limit: Int): Flux<FundingModel>
}
