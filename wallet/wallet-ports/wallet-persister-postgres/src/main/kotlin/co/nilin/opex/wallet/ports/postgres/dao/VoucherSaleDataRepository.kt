package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.VoucherSaleDataModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface VoucherSaleDataRepository : ReactiveCrudRepository<VoucherSaleDataModel, Long> {

    @Query(
        "select exists(select 1 from voucher_sale_data where voucher = :voucherId)"
    )
    fun existVoucherSaleData(voucherId: Long): Mono<Boolean>

    fun findByVoucher(voucherId: Long): Mono<VoucherSaleDataModel>
}