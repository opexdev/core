package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.CompanyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CompanyRepository : ReactiveCrudRepository<CompanyModel, Long> {

    @Query("select * from company where registration_number = :registrationNumber or national_id = :nationalId")
    fun findByRegistrationNumberOrNationalId(registrationNumber: String, nationalId: String): Mono<CompanyModel>

    @Query(
        """
        select c.*
        from company c
                 join company_representative r on r.company_id = c.id
        where r.uuid = :uuid
    """
    )
    fun findByRepresentativeUuid(uuid: String): Mono<CompanyModel>
}
