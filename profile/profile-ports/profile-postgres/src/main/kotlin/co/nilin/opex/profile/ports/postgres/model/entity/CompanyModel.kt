package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.Company
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("company")
data class CompanyModel(
    @Id var id: Long? = null
) : Company()
