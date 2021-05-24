package co.nilin.mixchange.port.eventlog.postgres.dao

import co.nilin.mixchange.port.eventlog.postgres.model.TradeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeRepository: ReactiveCrudRepository<TradeModel, Long>{
}