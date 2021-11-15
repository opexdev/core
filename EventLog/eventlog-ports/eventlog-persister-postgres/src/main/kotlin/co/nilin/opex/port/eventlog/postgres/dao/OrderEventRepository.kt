package co.nilin.opex.port.eventlog.postgres.dao

import co.nilin.opex.port.eventlog.postgres.model.OrderEventsModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderEventRepository : ReactiveCrudRepository<OrderEventsModel, Long>
