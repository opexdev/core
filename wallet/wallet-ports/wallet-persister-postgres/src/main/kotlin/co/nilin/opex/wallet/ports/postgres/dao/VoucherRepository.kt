package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.VoucherModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface VoucherRepository : ReactiveCrudRepository<VoucherModel, Long> {

    fun findByPublicCode(code: String): Mono<VoucherModel>
    fun findByPrivateCode(code: String): Mono<VoucherModel>
}