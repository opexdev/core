package co.nilin.opex.referral.ports.postgres.dao

import co.nilin.opex.referral.core.model.PaymentStatuses
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("payment_status")
data class PaymentStatus(@Id var status: PaymentStatuses)
