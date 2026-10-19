package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.CompanyRepresentative
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("company_representative")
data class CompanyRepresentativeModel(
    @Id var id: Long? = null
) : CompanyRepresentative()
