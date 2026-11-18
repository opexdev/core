package co.nilin.opex.wallet.core.spi
import java.math.BigDecimal
import java.time.LocalDateTime

interface DepositEventSubmitter {
    suspend fun send(
        uuid: String,
        depositRef: String?,
        depositUuid: String? = null,
        lifecycleEventId: String? = null,
        walletTransactionRef: String? = null,
        businessReference: String? = null,
        currency: String,
        amount: BigDecimal,
        principalAmount: BigDecimal? = null,
        appliedCustomerFee: BigDecimal? = null,
        feeAsset: String? = null,
        transferCategory: String? = null,
        status: String? = null,
        createDate: LocalDateTime? = LocalDateTime.now(),
        lifecycleTransitionDate: LocalDateTime? = null,
        completionDate: LocalDateTime? = null
    )
}