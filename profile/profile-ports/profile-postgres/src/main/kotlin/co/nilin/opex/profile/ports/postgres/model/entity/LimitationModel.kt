package co.nilin.opex.profile.ports.postgres.model.entity

import co.nilin.opex.profile.ports.postgres.model.base.Limitation
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("limitation")
data class LimitationModel(@Id var id: Long) : Limitation()
