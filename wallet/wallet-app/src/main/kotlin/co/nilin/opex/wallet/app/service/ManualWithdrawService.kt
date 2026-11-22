package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.core.inout.GatewayData
import co.nilin.opex.wallet.core.inout.OffChainGatewayCommand
import co.nilin.opex.wallet.core.inout.TransferMethod
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.service.WithdrawService
import co.nilin.opex.wallet.core.spi.WithdrawPersister
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class ManualWithdrawService(
    private val transferService: TransferService,
    private val withdrawPersister: WithdrawPersister,
    private val currencyServiceV2: CurrencyServiceV2,
    private val withdrawService: WithdrawService,
) {
    private val logger = LoggerFactory.getLogger(ManualWithdrawService::class.java)

    @Transactional
    suspend fun withdrawManually(
        symbol: String,
        receiverUuid: String,
        sourceUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest,
    ): TransferResult {
        logger.info("withdraw manually: $sourceUuid to $receiverUuid on $symbol at ${LocalDateTime.now()}")

        currencyServiceV2.fetchCurrencyGateway(request.gatewayUuid, symbol)
            ?.takeIf { it is OffChainGatewayCommand && it.transferMethod == TransferMethod.MANUALLY }
            ?.let {
                val gatewayData = GatewayData(
                    it.isWithdrawActive ?: true && it.withdrawAllowed ?: true,
                    it.withdrawFee ?: BigDecimal.ZERO,
                    it.withdrawMin ?: BigDecimal.ZERO,
                    it.withdrawMax
                )
                if (!gatewayData.isEnabled)
                    throw OpexError.WithdrawNotAllowed.exception()

                if (amount < gatewayData.minimum)
                    throw OpexError.WithdrawAmountLessThanMinimum.exception()

                if (amount > gatewayData.maximum)
                    throw OpexError.WithdrawAmountGreaterThanMaximum.exception()

            }
            ?: throw OpexError.GatewayNotFount.exception()

        val operationId = UUID.randomUUID().toString()
        val tx = transferService.transfer(
            symbol,
            WalletType.MAIN,
            sourceUuid,
            WalletType.MAIN,
            receiverUuid,
            amount,
            request.description,
            request.ref,
            TransferCategory.WITHDRAW_MANUALLY,
            operationId = operationId,
            transferMethod = TransferMethod.MANUALLY,
            applicator = receiverUuid,
        )
        //todo need to review

        val now = LocalDateTime.now()
        val withdrawal = withdrawPersister.persist(
            Withdraw(withdrawUuid = operationId, ownerUuid = sourceUuid, currency = symbol,
                wallet = tx.transferResult.destWallet!!, amount = amount,
                requestTransaction = null, finalizedTransaction = tx.tx,
                appliedFee = BigDecimal.ZERO, destAmount = amount, destSymbol = symbol,
                destAddress = receiverUuid, destNote = request.description, destTransactionRef = request.ref,
                status = WithdrawStatus.DONE, applicator = receiverUuid, withdrawType = WithdrawType.OFF_CHAIN,
                attachment = request.attachment, createDate = now, lastUpdateDate = now,
                transferMethod = TransferMethod.MANUALLY, otpRequired = 0, gatewayUuid = request.gatewayUuid)
        )
        withdrawService.publishWithdrawEvent(withdrawal, WithdrawStatus.DONE, tx.transferResult)
        return tx.transferResult;
    }


}