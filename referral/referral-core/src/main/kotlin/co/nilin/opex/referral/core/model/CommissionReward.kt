package co.nilin.opex.referral.core.model

data class CommissionReward<T>(var referrerUid: String, var referentUid: String, var tradeId: Long, var data: T)
