package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("references")
data class Reference(
    @Id var id: Long?,
    var uuid: String,
    var referralCodeId: Long
)
