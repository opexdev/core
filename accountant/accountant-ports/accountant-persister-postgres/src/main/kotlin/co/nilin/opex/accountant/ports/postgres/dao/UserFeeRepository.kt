package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.UserFeeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserFeeRepository : ReactiveCrudRepository<UserFeeModel, Long> {

    fun findByUuidAndQuoteSymbol(uuid: String, quoteSymbol: String): Mono<UserFeeModel>
}