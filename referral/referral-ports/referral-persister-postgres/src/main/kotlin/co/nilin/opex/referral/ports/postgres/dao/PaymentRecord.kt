package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("payment_records")
data class PaymentRecord(
    @Id var id: Long?,
    var commission_rewards_id: Long,
    var update_date: Long,
    var payment_status: String
)
