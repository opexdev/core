package co.nilin.opex.referral.core.spi

import co.nilin.opex.referral.core.model.CommissionReward

interface CommissionRewardHandler {
    suspend fun findAllCommissions(): List<CommissionReward>
    suspend fun findCommissionsByReferrer(uuid: String, code: String?): List<CommissionReward>
    suspend fun findCommissionsByReferent(uuid: String): List<CommissionReward>
    suspend fun deleteCommissionsByReferrer(uuid: String)
    suspend fun deleteCommissionsByReferent(uuid: String)
    suspend fun deleteCommissionsByCode(code: String)
    suspend fun deleteAllCommissions()
    suspend fun deleteCommissionById(id: Long)
}
