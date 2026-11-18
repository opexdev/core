package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.PublishedEventModel
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface PublishedEventRepository : ReactiveCrudRepository<PublishedEventModel, Long> {
    fun findByEventTimestampBetweenOrderByEventTimestampDesc(
        from: LocalDateTime,
        to: LocalDateTime,
        pageable: Pageable
    ): Flux<PublishedEventModel>
}
