package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.CompanyRepresentativeModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CompanyRepresentativeRepository : ReactiveCrudRepository<CompanyRepresentativeModel, Long> {

    fun findByUuid(uuid: String): Mono<CompanyRepresentativeModel>
}
