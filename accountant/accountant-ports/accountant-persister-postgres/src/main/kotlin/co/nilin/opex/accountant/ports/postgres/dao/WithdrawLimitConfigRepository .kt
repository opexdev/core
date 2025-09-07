package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.WithdrawLimitConfigModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WithdrawLimitConfigRepository : ReactiveCrudRepository<WithdrawLimitConfigModel, Long> {

    fun findByUserLevel(userLevel: String): Mono<WithdrawLimitConfigModel>
}