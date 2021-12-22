package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("commission_rewards")
data class CommissionReward(
    @Id var id: Long?,
    var referrerUuid: String,
    var referentUuid: String,
    var referralCode: String,
    var richTradeId: Long,
    var referrerShare: BigDecimal,
    var referentShare: BigDecimal,
    var checkoutDate: Long?,
) {
    val isCheckedOut: Boolean get() = checkoutDate != null
}
