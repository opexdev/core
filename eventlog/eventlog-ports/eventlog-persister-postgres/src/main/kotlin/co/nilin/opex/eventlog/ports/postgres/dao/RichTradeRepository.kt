package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.RichTradeModel
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

interface RichTradeRepository : ReactiveCrudRepository<RichTradeModel, Long> {
    fun findByExecutionReference(executionReference: String): Mono<RichTradeModel>
    fun findBySourceTopicAndSourcePartitionAndSourceOffset(topic: String, partition: Int, offset: Long): Mono<RichTradeModel>
    fun findByTradeDateTimeBetweenOrderByTradeDateTimeDesc(from: LocalDateTime, to: LocalDateTime, page: Pageable): Flux<RichTradeModel>
}
