package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("payment_status")
data class PaymentStatus(
    @Id var status: String?
)
