package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.VoucherGroupModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoucherGroupRepository : ReactiveCrudRepository<VoucherGroupModel, Long> {

}