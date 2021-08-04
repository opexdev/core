package co.nilin.opex.port.wallet.postgres.dao

import co.nilin.opex.port.wallet.postgres.model.WalletOwnerModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WalletOwnerRepository : ReactiveCrudRepository<WalletOwnerModel, Long> {
    @Query("select * from wallet_owner where uuid = :uuid")
    fun findByUuid(@Param("uuid") uuid: String): Mono<WalletOwnerModel>
}