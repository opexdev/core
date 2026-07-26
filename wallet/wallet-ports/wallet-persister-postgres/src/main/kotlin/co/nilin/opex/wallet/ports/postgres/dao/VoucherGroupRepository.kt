package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.VoucherGroupModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface VoucherGroupRepository : ReactiveCrudRepository<VoucherGroupModel, Long> {

    @Query(
        """
    UPDATE voucher_group 
    SET remaining_usage = :remainingUsage, version = version + 1
    WHERE id = :id AND version = :version
    RETURNING *
    """
    )
    fun updateRemainingWithVersion(id: Long, remainingUsage: Int, version: Long): Mono<VoucherGroupModel>

}