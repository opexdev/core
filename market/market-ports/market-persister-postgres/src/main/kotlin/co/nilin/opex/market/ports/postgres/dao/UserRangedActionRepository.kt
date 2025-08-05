package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.ports.postgres.model.UserRangedAction
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRangedActionRepository : ReactiveCrudRepository<UserRangedAction, Long> {
}