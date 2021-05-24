package co.nilin.mixchange.port.eventlog.postgres.dao

import co.nilin.mixchange.port.eventlog.postgres.model.OrderEventsModel
import co.nilin.mixchange.port.eventlog.postgres.model.OrderModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderEventRepository: ReactiveCrudRepository<OrderEventsModel, Long> {
}