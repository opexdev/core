package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.DeadLetterEventModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DeadLetterEventRepository : ReactiveCrudRepository<DeadLetterEventModel, Long> {
}