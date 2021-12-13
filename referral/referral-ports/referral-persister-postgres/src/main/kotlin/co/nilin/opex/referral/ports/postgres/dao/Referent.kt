package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("referents")
data class Referent(
    @Id var id: Long?,
    var uuid: String,
    var referral_code_id: Long
)
