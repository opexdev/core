package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.VoucherData
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
    SELECT v.public_code,
           v.amount,
           v.currency,
           v.expire_date,
           v.create_date,
           vg.type,
           vg.issuer,
           vg.description,
           vg.status                                                             as group_status,
           (select count(*) from voucher_usage where voucher = v.id) as usages_count

    from voucher v
             inner join voucher_group vg on v.voucher_group = vg.id
             inner join voucher_usage vu on v.id = vu.voucher
    where public_code = :code
    GROUP BY v.id, vg.id
    """
    )
    fun findVoucherWithRelationsByPublicCode(code: String): Mono<VoucherData>

    @Query(
        """
    SELECT v.public_code,
           v.amount,
           v.currency,
           v.expire_date,
           v.create_date,
           vg.type,
           vg.issuer,
           vg.description,
           vg.status                                                             as group_status,
           (select count(*) from voucher_usage where voucher = v.id) as usages_count

    from voucher v
             inner join voucher_group vg on v.voucher_group = vg.id
             left join voucher_usage vu on v.id = vu.voucher
      WHERE (:type IS NULL OR vg.type = :type)
      AND (:issuer IS NULL OR vg.issuer = :issuer)
      AND (:isUsed IS NULL OR (:isUsed = true AND vu IS NOT NULL) OR (:isUsed = false AND vu IS NULL)) 
    GROUP BY v.id, vg.id
    limit :limit
    offset :offset
    """
    )
    fun findAll(
        type: VoucherGroupType?,
        issuer: String?,
        isUsed: Boolean?,
        limit: Int? = 0,
        offset: Int? = 10000,
    ): Flow<VoucherData>
}