package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("referrals")
data class Referral(
    @Id var id: Long?,
    var uuid: String,
    var code: String,
    var parent: Long
)
