package co.nilin.mixchange.port.accountant.postgres.dao

import co.nilin.mixchange.port.accountant.postgres.model.PairConfigModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PairConfigRepository: ReactiveCrudRepository<PairConfigModel, String> {
}