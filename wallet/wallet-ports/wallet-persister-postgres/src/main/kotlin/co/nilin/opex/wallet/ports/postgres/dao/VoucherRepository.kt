package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.VoucherGroupStatus
import co.nilin.opex.wallet.core.model.VoucherGroupType
import co.nilin.opex.wallet.ports.postgres.model.VoucherModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface VoucherRepository : ReactiveCrudRepository<VoucherModel, Long> {

    fun findByPublicCode(code: String): Mono<VoucherModel>
    fun findByPrivateCode(code: String): Mono<VoucherModel>

    @Query(
        """
    select * from voucher v 
    inner join voucher_group vg on v.voucher_group = vg.id
    where (:type is null or vg.type = :type)  order by create_date desc
    limit :limit
    offset :offset 
    """
    )
    fun findAll(type: VoucherGroupType?, limit: Int? = 0, offset: Int? = 10000): Flow<VoucherModel>
}