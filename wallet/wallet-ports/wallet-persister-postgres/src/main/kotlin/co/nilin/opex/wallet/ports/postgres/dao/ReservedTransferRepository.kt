package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.ReservedTransferModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ReservedTransferRepository : ReactiveCrudRepository<ReservedTransferModel, Long> {
    fun findByReserveNumber(reservedNumber: String): Mono<ReservedTransferModel>?

    @Query(
        """
        SELECT * from reserved_Transfer
        """
    )
    fun findByReservedTransfer(): Flux<ReservedTransferModel>
}