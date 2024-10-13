package co.nilin.opex.wallet.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.core.inout.DepositResponse
import co.nilin.opex.wallet.app.dto.ManualTransferRequest
import co.nilin.opex.wallet.core.inout.Deposit
import co.nilin.opex.wallet.core.inout.GatewayType
import co.nilin.opex.wallet.core.inout.TransferResult
import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.DepositType
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class DepositService(
        private val walletOwnerManager: WalletOwnerManager,
        private val currencyService: CurrencyServiceV2,
        private val depositPersister: DepositPersister,
        private val transferService: TransferService
) {
    private val logger = LoggerFactory.getLogger(DepositService::class.java)

    @Transactional
    suspend fun depositManually(
            symbol: String,
            receiverUuid: String,
            senderUuid: String,
            amount: BigDecimal,
            request: ManualTransferRequest
    ): TransferResult {
        logger.info("deposit manually: $senderUuid to $receiverUuid on $symbol at ${LocalDateTime.now()}")
        val systemUuid = "1"
        //todo customize error message
        if (!isManualWithdrawAllowed(symbol))
            throw OpexError.GatewayNotFount.exception()
        val senderLevel = walletOwnerManager.findWalletOwner(senderUuid)?.let { it.level }
                ?: throw OpexException(OpexError.WalletOwnerNotFound)
        walletOwnerManager.findWalletOwner(receiverUuid)?.let { it.level }
                ?: walletOwnerManager.createWalletOwner(
                        receiverUuid,
                        "not set",
                        "1"
                ).level


        val tx = transferService.transfer(
                symbol,
                WalletType.MAIN,
                senderUuid,
                WalletType.MAIN,
                receiverUuid,
                amount,
                request.description,
                request.ref,
                TransferCategory.DEPOSIT_MANUALLY,

                )
        depositPersister.persist(
                Deposit(
                        receiverUuid,
                        UUID.randomUUID().toString(),
                        symbol,
                        amount,
                        note = request.description,
                        transactionRef = request.ref,
                        status = DepositStatus.DONE,
                        depositType = DepositType.MANUALLY,
                )
        )
        return tx
    }


    @Transactional
    suspend fun deposit(
            symbol: String,
            receiverUuid: String,
            receiverWalletType: WalletType,
            amount: BigDecimal,
            description: String?,
            transferRef: String?,
            chain: String?
    ): TransferResult {

        val tx = transferService.transfer(
                symbol,
                WalletType.MAIN,
                walletOwnerManager.systemUuid,
                receiverWalletType,
                receiverUuid,
                amount,
                description,
                transferRef,
                TransferCategory.DEPOSIT,
        )

        depositPersister.persist(
                Deposit(
                        receiverUuid,
                        UUID.randomUUID().toString(),
                        symbol,
                        amount,
                        note = description,
                        transactionRef = transferRef,
                        status = DepositStatus.DONE,
                        depositType = DepositType.ON_CHAIN,
                        network = chain
                )
        )

        return tx
    }

    internal suspend fun isManualWithdrawAllowed(symbol: String): Boolean {
        return currencyService.fetchCurrencyWithGateways(symbol, listOf(GatewayType.Manually))?.withdrawAllowed ?: false
    }


    suspend fun findDepositHistory(
            uuid: String,
            symbol: String?,
            startTime: LocalDateTime?,
            endTime: LocalDateTime?,
            limit: Int?,
            size: Int?,
            ascendingByTime: Boolean?
    ): List<DepositResponse> {
        return depositPersister.findDepositHistory(uuid, symbol, startTime, endTime, limit, size, ascendingByTime).map {
            DepositResponse(
                    it.id!!,
                    it.ownerUuid,
                    it.currency,
                    it.amount,
                    it.network,
                    it.note,
                    it.transactionRef,
                    it.sourceAddress,
                    it.status,
                    it.depositType,
                    it.createDate
            )
        }
    }


    suspend fun searchDeposit(
            ownerUuid: String?,
            symbol: String?,
            sourceAddress: String?,
            transactionRef: String?,
            startTime: LocalDateTime?,
            endTime: LocalDateTime?,
            offset: Int?,
            size: Int?,
            ascendingByTime: Boolean?
    ): List<DepositResponse> {

        return depositPersister.findByCriteria(
                ownerUuid,
                symbol,
                sourceAddress,
                transactionRef,
                startTime,
                endTime,
                offset,
                size,
                ascendingByTime
        ).map {
            DepositResponse(
                    it.id!!,
                    it.ownerUuid,
                    it.currency,
                    it.amount,
                    it.network,
                    it.note,
                    it.transactionRef,
                    it.sourceAddress,
                    it.status,
                    it.depositType,
                    it.createDate
            )
        }
    }
}