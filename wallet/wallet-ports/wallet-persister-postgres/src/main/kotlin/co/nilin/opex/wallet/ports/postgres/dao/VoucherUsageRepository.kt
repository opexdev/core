package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.VoucherUsageModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface VoucherUsageRepository : ReactiveCrudRepository<VoucherUsageModel, Long> {

    @Query(
        "select exists(select 1 from voucher_usage where voucher = :voucherId)"
    )
    fun existsVoucherUsage(voucherId: Long): Mono<Boolean>

    @Query(
        """
        select count(*) from voucher_usage vu
            inner join voucher v on v.id = vu.voucher
            inner join voucher_group vg on v.voucher_group = vg.id
                where vu.uuid = :uuid and vg.id = :voucherGroupId
        """
    )
    fun count(uuid: String, voucherGroupId: Long): Mono<Long>

    @Query(
        """
        select count(*) from voucher_usage
            where voucher = :voucherId
        """
    )
    fun count(voucherId : Long): Mono<Long>

}