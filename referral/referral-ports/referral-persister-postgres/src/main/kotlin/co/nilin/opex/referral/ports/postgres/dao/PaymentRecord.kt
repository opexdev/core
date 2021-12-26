package co.nilin.opex.referral.ports.postgres.dao

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.referral.core.model.PaymentStatuses
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("payment_records")
data class PaymentRecord(
    @Id var id: Long?,
    var commissionRewardsId: Long,
    var createDate: Long,
    var paymentStatus: PaymentStatuses
)

data class PaymentRecordProjected(
    @Id var id: Long?,
    var referrerUuid: String,
    var referentUuid: String,
    var referralCode: String,
    var richTradeId: Long,
    var referentOrderDirection: OrderDirection,
    var referrerShare: BigDecimal,
    var referentShare: BigDecimal,
    var createDate: Long,
    var paymentStatus: PaymentStatuses
)