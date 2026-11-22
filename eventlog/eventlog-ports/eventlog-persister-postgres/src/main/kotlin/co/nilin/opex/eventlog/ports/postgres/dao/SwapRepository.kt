package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.SwapModel
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

interface SwapRepository : ReactiveCrudRepository<SwapModel, Long> {
    fun findByTxId(txId: String): Mono<SwapModel>
    fun findBySourceTopicAndSourcePartitionAndSourceOffset(topic: String, partition: Int, offset: Long): Mono<SwapModel>
    fun findByCreateDateBetweenOrderByCreateDateDesc(from: LocalDateTime, to: LocalDateTime, page: Pageable): Flux<SwapModel>
}
