package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.CompanyModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface CompanyRepository : ReactiveCrudRepository<CompanyModel, Long> {

    @Query("select * from company where registration_number = :registrationNumber or national_id = :nationalId limit 1")
    fun findByRegistrationNumberOrNationalId(registrationNumber: String, nationalId: String): Mono<CompanyModel>

    fun findByCompanyUuid(companyUuid: UUID): Mono<CompanyModel>

    fun findByNationalId(nationalId: String): Mono<CompanyModel>

    fun findByRegistrationNumber(registrationNumber: String): Mono<CompanyModel>

    @Query(
        """
        select c.*
        from company c
                 join company_representative r on r.company_id = c.id
        where r.uuid = :uuid
    """
    )
    fun findByRepresentativeUuid(uuid: String): Mono<CompanyModel>

    @Query(
        """
        select distinct c.*
        from company c
                 left join company_representative r on r.company_id = c.id
        where (:companyName is null or lower(c.name) like lower(concat('%', :companyName, '%')))
           and (:representativeName is null or lower(concat(r.first_name, ' ', r.last_name)) like lower(concat('%', :representativeName, '%')))
           and (:nationalId is null or c.national_id = :nationalId)
           and (:registrationNumber is null or c.registration_number = :registrationNumber)
           and (:createDateFrom is null or c.create_date >= to_timestamp(:createDateFrom / 1000.0))
           and (:createDateTo is null or c.create_date <= to_timestamp(:createDateTo / 1000.0))
        order by c.create_date desc
        limit :limit offset :offset
    """
    )
    fun searchCompanies(
        companyName: String?,
        representativeName: String?,
        nationalId: String?,
        registrationNumber: String?,
        createDateFrom: Long?,
        createDateTo: Long?,
        limit: Int,
        offset: Int
    ): reactor.core.publisher.Flux<CompanyModel>
}

