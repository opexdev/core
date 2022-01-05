package co.nilin.opex.referral.ports.postgres.dao

import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.referral.core.model.CheckoutState
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("payment_records")
data class CheckoutRecord(
    @Id var id: Long?,
    var commissionRewardsId: Long,
    var transferRef: String?,
    var updateDate: LocalDateTime,
    var checkoutState: CheckoutState
)

data class CheckoutRecordProjected(
    @Id var id: Long?,
    var commissionRewardsId: Long,
    var rewardedUuid: String,
    var referentUuid: String,
    var referralCode: String,
    var richTradeId: Long,
    var referentOrderDirection: OrderDirection,
    var share: BigDecimal,
    var transferRef: String?,
    var createDate: LocalDateTime,
    var updateDate: LocalDateTime,
    var checkoutState: CheckoutState
)