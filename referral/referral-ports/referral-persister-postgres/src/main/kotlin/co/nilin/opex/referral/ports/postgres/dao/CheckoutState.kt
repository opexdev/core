package co.nilin.opex.referral.ports.postgres.dao

import co.nilin.opex.referral.core.model.CheckoutState
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("checkout_status")
data class CheckoutState(@Id var state: CheckoutState)
