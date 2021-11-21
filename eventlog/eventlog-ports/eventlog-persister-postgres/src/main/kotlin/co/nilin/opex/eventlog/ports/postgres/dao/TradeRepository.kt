package co.nilin.opex.eventlog.ports.postgres.dao

import co.nilin.opex.eventlog.ports.postgres.model.TradeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeRepository : ReactiveCrudRepository<TradeModel, Long>
