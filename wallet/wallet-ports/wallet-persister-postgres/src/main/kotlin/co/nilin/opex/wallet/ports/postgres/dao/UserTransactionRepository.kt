package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.UserTransactionCategory
import co.nilin.opex.wallet.core.model.UserTransactionHistory
import co.nilin.opex.wallet.ports.postgres.model.UserTransactionModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface UserTransactionRepository : ReactiveCrudRepository<UserTransactionModel, Long> {

    @Query(
        """
        select ut.uuid as id, o.uuid as user_id, currency, balance_change, balance_before, category, description, date 
        from user_transaction ut
        join wallet_owner o on o.id = ut.owner_id
        where o.uuid = :userId
            and (:currency is null or currency = :currency)
            and (:category is null or category = :category)
            and (:startTime is null or date > :startTime)
            and (:endTime is null or date <= :endTime)
        order by date
        limit :limit
        offset :offset
    """
    )
    fun findUserTransactionHistoryAsc(
        userId: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flux<UserTransactionHistory>

    @Query(
        """
        select ut.uuid as id, o.uuid as user_id, currency, balance_change, balance_before, category, description, date
        from user_transaction ut
        join wallet_owner o on o.id = ut.owner_id
        where o.uuid = :userId
            and (:currency is null or currency = :currency)
            and (:category is null or category = :category)
            and (:startTime is null or date > :startTime)
            and (:endTime is null or date <= :endTime)
        order by date desc
        limit :limit
        offset :offset
    """
    )
    fun findUserTransactionHistoryDesc(
        userId: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flux<UserTransactionHistory>
}