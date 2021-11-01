package co.nilin.opex.port.eventlog.postgres.dao

import co.nilin.opex.port.eventlog.postgres.model.TradeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeRepository : ReactiveCrudRepository<TradeModel, Long>
