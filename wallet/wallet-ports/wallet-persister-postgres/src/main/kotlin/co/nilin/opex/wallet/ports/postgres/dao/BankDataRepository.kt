package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.BankDataModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BankDataRepository : ReactiveCrudRepository<BankDataModel, Long> {
    fun findByIdentifier(identifier: String): Mono<BankDataModel>?
    fun findByUuid(uuid: String): Mono<BankDataModel>?


}