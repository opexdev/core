package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.ports.postgres.model.FinancialActionModel
import co.nilin.opex.accountant.ports.postgres.model.FinancialActionRetryModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface FinancialActionRetryRepository : ReactiveCrudRepository<FinancialActionRetryModel, Long> {

    @Query(
        """
        update fi_action_retry 
        set retries = :retries, next_run_time = :runTime, has_given_up = :hasGivenUp
        where id = :id
    """
    )
    fun scheduleNext(id: Long, retries: Int, runTime: LocalDateTime, hasGivenUp: Boolean): Mono<Int>

    @Query("update fi_action_retry set is_resolved = true where fa_id = :faId")
    fun updateResolvedTrue(faId: Long): Mono<Int>

    @Query("select * from fi_action_retry where fa_id = :faId")
    fun findByFaId(faId: Long): Mono<FinancialActionRetryModel>

    @Query(
        """
        select fa.* from fi_action_retry far
        join fi_actions fa on fa.id = far.fa_id
        where next_run_time < :time and has_given_up = false and is_resolved = false
        order by next_run_time
        limit :limit
    """
    )
    fun findAllRetries(time: LocalDateTime, limit: Int): Flux<FinancialActionModel>
}