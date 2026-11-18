package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.model.WithdrawStatus
import java.math.BigDecimal
import java.time.LocalDateTime

interface WithdrawRequestEventSubmitter {
    //It is temporary class
    suspend fun send(
        uuid: String,
        withdrawId: Long?,
        withdrawUuid: String? = null,
        lifecycleEventId: String? = null,
        walletTransactionRef: String? = null,
        businessReference: String? = null,
        currency: String,
        amount: BigDecimal,
        principalAmount: BigDecimal? = null,
        appliedCustomerFee: BigDecimal? = null,
        feeAsset: String? = null,
        transferCategory: String? = null,
        withdrawStatus: WithdrawStatus,
        createDate: LocalDateTime,
        lifecycleTransitionDate: LocalDateTime? = null,
        completionDate: LocalDateTime? = null,
        counterpartyReferenceId: String? = null
    )
}