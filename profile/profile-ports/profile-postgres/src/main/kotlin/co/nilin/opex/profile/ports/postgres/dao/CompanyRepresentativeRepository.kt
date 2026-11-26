package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.CompanyRepresentativeModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CompanyRepresentativeRepository : ReactiveCrudRepository<CompanyRepresentativeModel, Long> {

    fun findByUuid(uuid: String): Mono<CompanyRepresentativeModel>

    fun findByCompanyId(companyId: Long): Flux<CompanyRepresentativeModel>

    @Query(
        """
        select r.uuid, p.first_name, p.last_name
        from company_representative r
        join profile p on p.user_id = r.uuid
        where r.company_id = :companyId
        order by r.create_date asc
    """
    )
    fun findRepresentativesWithNames(companyId: Long): Flux<RepresentativeNameDto>
}

data class RepresentativeNameDto(
    val uuid: String,
    val first_name: String,
    val last_name: String
)
