package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.VoucherGroupModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface VoucherGroupRepository : ReactiveCrudRepository<VoucherGroupModel, Long> {

    @Query("update voucher_group set remaining_usage = :remainingUsage  where id = :id")
    fun updateRemaining(id: Long, remainingUsage: Int): Mono<Int>

}