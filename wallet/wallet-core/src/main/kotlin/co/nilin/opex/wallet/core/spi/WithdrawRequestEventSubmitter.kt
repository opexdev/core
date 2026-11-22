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
        counterpartyReferenceId: String? = null,
        transferMethod: String? = null,
        operationType: String? = null,
        applicator: String? = null,
        gatewayId: String? = null,
        network: String? = null,
        note: String? = null,
        attachment: String? = null,
        senderPartyId: String? = null,
        receiverPartyId: String? = null,
        senderWalletId: String? = null,
        receiverWalletId: String? = null,
        senderWalletType: String? = null,
        receiverWalletType: String? = null,
        destinationAddress: String? = null,
        destinationAsset: String? = null,
        destinationAmount: BigDecimal? = null,
        statusReason: String? = null,
        requestTransactionId: String? = null,
        finalizedTransactionId: String? = null,
        operationWalletId: String? = null,
        otpRequired: Int? = null
    )
}