package co.nilin.mixchange.port.eventlog.postgres.dao

import co.nilin.mixchange.port.eventlog.postgres.model.EventModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository: ReactiveCrudRepository<EventModel, Long> {
}