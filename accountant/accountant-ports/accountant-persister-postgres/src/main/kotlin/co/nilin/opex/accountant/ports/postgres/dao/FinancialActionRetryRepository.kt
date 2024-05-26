package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.FinancialActionRetryModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface FinancialActionRetryRepository : ReactiveCrudRepository<FinancialActionRetryModel, Long> {

    @Query("select * from fi_action_retry where fa_id = :faId")
    fun findByFaId(faId: Long): Mono<FinancialActionRetryModel>
}