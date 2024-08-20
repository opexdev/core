package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.ManualGatewayModel
import co.nilin.opex.wallet.ports.postgres.model.RateModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ManualGatewayRepository : ReactiveCrudRepository<ManualGatewayModel, Long> {
}