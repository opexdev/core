package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.TradeModel
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface TradeRepository : ReactiveCrudRepository<TradeModel, Long> {
    fun findByEventDateBetweenOrderByEventDateDesc(
        from: LocalDateTime,
        to: LocalDateTime,
        pageable: Pageable
    ): Flux<TradeModel>
}
