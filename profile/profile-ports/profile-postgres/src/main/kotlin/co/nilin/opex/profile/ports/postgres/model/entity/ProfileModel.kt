package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("profile")
data class ProfileModel(
    @Id var id: Long
) : Profile()
