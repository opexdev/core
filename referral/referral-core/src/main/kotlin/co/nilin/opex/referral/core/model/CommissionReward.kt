package co.nilin.opex.referral.core.model

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.matching.engine.core.model.OrderDirection
import java.math.BigDecimal

data class CommissionReward(
    var referrerUuid: String,
    var referentUuid: String,
    var referralCode: String,
    var richTrade: Pair<Long, RichTrade?>,
    var referentOrderDirection: OrderDirection,
    var referrerShare: BigDecimal,
    var referentShare: BigDecimal,
    var paymentAssetSymbol: String
)
