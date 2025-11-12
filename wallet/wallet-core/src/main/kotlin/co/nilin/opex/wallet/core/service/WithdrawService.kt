package co.nilin.opex.wallet.core.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.security.JwtUtils
import co.nilin.opex.wallet.core.inout.*
import co.nilin.opex.wallet.core.inout.otp.*
import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.model.WithdrawType
import co.nilin.opex.wallet.core.spi.*
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

@Service
class WithdrawService(
    private val withdrawPersister: WithdrawPersister,
    private val walletManager: WalletManager,
    private val walletOwnerManager: WalletOwnerManager,
    private val currencyService: CurrencyServiceManager,
    private val transferManager: TransferManager,
    private val meterRegistry: MeterRegistry,
    private val gatewayService: GatewayService,
    private val precisionService: PrecisionService,
    private val accountantProxy: AccountantProxy,
    private val withdrawRequestEventSubmitter: WithdrawRequestEventSubmitter,
    private val otpProxy: OtpProxy,
    private val profileProxy: ProfileProxy,
    private val withdrawOtpPersister: WithdrawOtpPersister,
    @Qualifier("onChainGateway") private val bcGatewayProxy: GatewayPersister,
    @Value("\${app.system.uuid}") private val systemUuid: String,
    @Value("\${app.withdraw.limit.enabled}") private val withdrawLimitEnabled: Boolean,
    @Value("\${app.withdraw.otp-required-count}") private val otpRequiredCount: Int,
    @Value("\${app.withdraw.bank-account-validation}") private val bankAccountValidation: Boolean,
) {
    private val logger = LoggerFactory.getLogger(WithdrawService::class.java)

    @Transactional
    suspend fun requestWithdraw(withdrawCommand: WithdrawCommand, token: String): WithdrawActionResult {
        precisionService.validatePrecision(withdrawCommand.amount, withdrawCommand.currency)
        val withdrawData: GatewayData =
            fetchWithdrawData(withdrawCommand) ?: throw OpexError.GatewayNotFount.exception()
        if (!withdrawData.isEnabled)
            throw OpexError.WithdrawNotAllowed.exception()
        if (bankAccountValidation)
            verifyOwnershipForWithdraw(token, withdrawCommand)

        val currency = currencyService.fetchCurrency(FetchCurrency(symbol = withdrawCommand.currency))
            ?: throw OpexError.CurrencyNotFound.exception()

        if (withdrawLimitEnabled) {
            val userRole =
                UserRole.getHighestRoleKeycloakName(JwtUtils.extractRoles(token)) ?: UserRole.USER_1.keycloakName
            val canWithdraw = accountantProxy.canRequestWithdraw(
                withdrawCommand.uuid,
                userRole,
                withdrawCommand.currency,
                withdrawCommand.amount
            )
            if (!canWithdraw) throw OpexError.WithdrawAmountExceeds.exception()
        }

        val owner = walletOwnerManager.findWalletOwner(withdrawCommand.uuid)
            ?: throw OpexError.WalletOwnerNotFound.exception()

        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
            ?: throw OpexError.WalletNotFound.exception()

        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.CASHOUT, currency)
            ?: walletManager.createWallet(owner, Amount(currency, BigDecimal.ZERO), currency, WalletType.CASHOUT)

        val withdrawFee = withdrawData.fee
        val realAmount = withdrawCommand.amount - withdrawFee

        when {
            withdrawCommand.amount > sourceWallet.balance.amount + withdrawFee ->
                throw OpexError.WithdrawAmountExceedsWalletBalance.exception()

            withdrawCommand.amount < withdrawData.minimum ->
                throw OpexError.WithdrawAmountLessThanMinimum.exception()

            withdrawCommand.amount > withdrawData.maximum ->
                throw OpexError.WithdrawAmountGreaterThanMaximum.exception()
        }

        val withdraw = withdrawPersister.persist(
            Withdraw(
                ownerUuid = owner.uuid,
                currency = currency.symbol,
                wallet = receiverWallet.id!!,
                amount = realAmount,
                appliedFee = withdrawFee,
                destSymbol = withdrawCommand.destSymbol,
                destAddress = withdrawCommand.destAddress,
                destNetwork = withdrawCommand.destNetwork,
                destNote = withdrawCommand.destNote,
                status = WithdrawStatus.REQUESTED,
                withdrawType = withdrawCommand.withdrawType!!,
                transferMethod = withdrawCommand.transferMethod,
                otpRequired = otpRequiredCount
            )
        )

        return if (otpRequiredCount <= 0) {
            createWithdraw(withdraw.withdrawId!!)
            WithdrawActionResult(withdraw.withdrawId, WithdrawStatus.CREATED, WithdrawNextAction.WAITING_FOR_ADMIN)
        } else {
            WithdrawActionResult(withdraw.withdrawId!!, WithdrawStatus.REQUESTED, WithdrawNextAction.fromOrder(1))
        }
    }

    suspend fun requestOTP(token: String, uuid: String, withdrawId: Long, otpType: OTPType): TempOtpResponse {
        val withdraw = withdrawPersister.findById(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()
        if (withdraw.ownerUuid != uuid) throw OpexError.Forbidden.exception()
        if (withdraw.status != WithdrawStatus.REQUESTED) {
            throw OpexError.OTPCannotBeRequested.exception()
        }
        withdrawOtpPersister.findByWithdrawId(withdrawId).find { t -> t.otpType == otpType }?.let {
            throw OpexError.OTPAlreadyRequested.exception("OTP with type : $otpType already exists for withdraw : $withdrawId")
        }
        if (Duration.between(withdraw.createDate, LocalDateTime.now()).toMinutes() > 10) {
            throw OpexError.WithdrawRequestExpired.exception("Withdraw request expired after 10 minutes")
        }
        val profile =
            profileProxy.getProfile(token) // TODO When the profile is restricted in nginx, remove the token and use the uuid.
        val receiver = when (otpType) {
            OTPType.SMS -> profile.mobile
            OTPType.EMAIL -> profile.email
        }
        val request = NewOTPRequest(
            userId = receiver,
            receivers = listOf(OTPReceiver(receiver, otpType)),
            action = OTPAction.WITHDRAW.name
        )
        return otpProxy.requestOTP(request).apply { receivers = listOf(OTPReceiver(receiver, otpType)) }
    }

    suspend fun verifyOTP(token: String, withdrawId: Long, otpType: OTPType, otpCode: String): WithdrawActionResult {
        val profile = profileProxy.getProfile(token)
        val (receiver, nextAction) = when (otpType) {
            OTPType.EMAIL -> Pair(profile.email, WithdrawNextAction.OTP_MOBILE)
            OTPType.SMS -> Pair(profile.mobile, WithdrawNextAction.OTP_EMAIL)
        }
        val verifyResponse = otpProxy.verifyOTP(
            VerifyOTPRequest(receiver, listOf(OTPCode(otpType, otpCode)))
        )
        if (!verifyResponse.result) {
            throw OpexError.InvalidOTP.exception()
        }
        withdrawOtpPersister.save(
            WithdrawOtp(withdrawId, verifyResponse.tracingCode!!, otpType, LocalDateTime.now())
        )
        val otpCount = withdrawOtpPersister.findByWithdrawId(withdrawId).size
        return if (otpCount == otpRequiredCount) {
            createWithdraw(withdrawId)
            WithdrawActionResult(withdrawId, WithdrawStatus.CREATED, WithdrawNextAction.WAITING_FOR_ADMIN)
        } else WithdrawActionResult(withdrawId, WithdrawStatus.REQUESTED, nextAction)


    }

    private suspend fun createWithdraw(withdrawId: Long) {
        val withdraw = withdrawPersister.findById(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()
        val currency = currencyService.fetchCurrency(FetchCurrency(symbol = withdraw.currency))
            ?: throw OpexError.CurrencyNotFound.exception()
        val owner =
            walletOwnerManager.findWalletOwner(withdraw.ownerUuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
            ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            owner, WalletType.CASHOUT, currency
        ) ?: walletManager.createWallet(
            owner, Amount(currency, BigDecimal.ZERO), currency, WalletType.CASHOUT
        )
        val transferResultDetailed = transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(currency, withdraw.amount),
                null,
                "wallet:withdraw:${owner.uuid}:${WithdrawStatus.CREATED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_REQUEST
            )
        )

        withdrawPersister.persist(withdraw.apply {
            withdraw.requestTransaction = transferResultDetailed.tx
            withdraw.status = WithdrawStatus.CREATED
            withdraw.lastUpdateDate = LocalDateTime.now()
        })

        publishWithdrawEvent(withdraw, WithdrawStatus.CREATED)
        incrementWithdrawMetric()
    }

    private suspend fun publishWithdrawEvent(withdraw: Withdraw, withdrawStatus: WithdrawStatus) {
        if (withdrawLimitEnabled)
            withdrawRequestEventSubmitter.send(
                withdraw.ownerUuid,
                withdraw.withdrawId!!,
                withdraw.currency,
                withdraw.amount,
                withdrawStatus,
                withdraw.createDate
            )
    }

    private fun incrementWithdrawMetric() {
        try {
            meterRegistry.counter("withdraw_request_event").increment()
        } catch (e: Exception) {
            logger.warn("error in incrementing withdraw_request_event counter", e)
        }
    }

    private suspend fun fetchWithdrawData(withdrawCommand: WithdrawCommand): GatewayData? {

        return withdrawCommand.gatewayUuid?.let { uuid ->

            gatewayService.fetchGateway(uuid, withdrawCommand.currency)?.let {

                when (it) {
                    is OnChainGatewayCommand -> {
                        withdrawCommand.currency = it.currencySymbol!!
                        withdrawCommand.destNetwork = it.chain
                        withdrawCommand.destSymbol = it.implementationSymbol!!
                        withdrawCommand.withdrawType = WithdrawType.ON_CHAIN
                        GatewayData(
                            it.isWithdrawActive ?: true && it.withdrawAllowed ?: true,
                            it.withdrawFee ?: BigDecimal.ZERO,
                            it.withdrawMin ?: BigDecimal.ZERO,
                            it.withdrawMax
                        )
                    }

                    is OffChainGatewayCommand -> {
                        withdrawCommand.currency = it.currencySymbol!!
                        withdrawCommand.destNetwork = it.transferMethod.name
                        withdrawCommand.withdrawType = WithdrawType.OFF_CHAIN
                        withdrawCommand.transferMethod = it.transferMethod
                        GatewayData(
                            it.isWithdrawActive ?: true && it.withdrawAllowed ?: true,
                            it.withdrawFee ?: BigDecimal.ZERO,
                            it.withdrawMin ?: BigDecimal.ZERO,
                            it.withdrawMax
                        )
                    }

                    else -> {
                        throw OpexError.GatewayNotFount.exception()
                    }
                }
            } ?: throw OpexError.GatewayNotFount.exception()

            //After applying gateway concept in ope, we can remove this line and
            // use gatewayUUid instead of combination of symbol and network
        } ?: bcGatewayProxy.getWithdrawData(withdrawCommand.destSymbol!!, withdrawCommand.destNetwork!!)
    }

    @Transactional
    suspend fun acceptWithdraw(acceptCommand: WithdrawAcceptCommand): WithdrawActionResult {
        val withdraw =
            withdrawPersister.findById(acceptCommand.withdrawId) ?: throw OpexError.WithdrawNotFound.exception()
        if (!withdraw.canBeAccepted()) throw OpexError.WithdrawCannotBeAccepted.exception()

        val system = walletOwnerManager.findWalletOwner(systemUuid) ?: throw OpexError.WalletOwnerNotFound.exception()

        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet =
            walletManager.findWalletByOwnerAndCurrencyAndType(system, WalletType.MAIN, sourceWallet.currency)
                ?: walletManager.createWallet(
                    system, Amount(sourceWallet.currency, BigDecimal.ZERO), sourceWallet.currency, WalletType.MAIN
                )

        val transferResultDetailed = transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, withdraw.amount + withdraw.appliedFee),
                null,
                "wallet:withdraw:${sourceWallet.owner.uuid}:${WithdrawStatus.ACCEPTED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_ACCEPT
            )
        )

        val updateWithdraw = withdrawPersister.persist(
            Withdraw(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.currency,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                transferResultDetailed.tx,
                withdraw.appliedFee,
                acceptCommand.destAmount ?: withdraw.amount,
                withdraw.destSymbol,
                withdraw.destAddress,
                withdraw.destNetwork,
                withdraw.destNote ?: acceptCommand.destNote,
                acceptCommand.destTransactionRef,
                null,
                WithdrawStatus.ACCEPTED,
                withdraw.applicator,
                withdraw.withdrawType,
                acceptCommand.attachment,
                withdraw.createDate,
                LocalDateTime.now(),
                withdraw.transferMethod,
                otpRequired = otpRequiredCount
            )
        )
        return WithdrawActionResult(updateWithdraw.withdrawId!!, WithdrawStatus.ACCEPTED)
    }


    @Transactional
    suspend fun cancelWithdraw(uuid: String, withdrawId: Long) {
        val withdraw = withdrawPersister.findById(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()
        if (withdraw.ownerUuid != uuid) throw OpexError.Forbidden.exception()
        if (!withdraw.canBeCanceled()) throw OpexError.WithdrawCannotBeCanceled.exception()

        val currency = currencyService.fetchCurrency(FetchCurrency(symbol = withdraw.currency))
            ?: throw OpexError.CurrencyNotFound.exception()
        val owner = walletOwnerManager.findWalletOwner(uuid) ?: throw OpexError.WalletOwnerNotFound.exception()
        val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.CASHOUT, currency)
            ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
            ?: throw OpexError.WalletNotFound.exception()

        withdrawPersister.persist(withdraw.apply {
            withdraw.status = WithdrawStatus.CANCELED
            withdraw.lastUpdateDate = LocalDateTime.now()
        })

        publishWithdrawEvent(withdraw, WithdrawStatus.CANCELED)


        transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(currency, withdraw.amount + withdraw.appliedFee),
                null,
                "wallet:withdraw:${withdraw.withdrawId}:${WithdrawStatus.CANCELED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_CANCEL
            )
        )
    }

    @Transactional
    suspend fun rejectWithdraw(rejectCommand: WithdrawRejectCommand): WithdrawActionResult {
        val withdraw =
            withdrawPersister.findById(rejectCommand.withdrawId) ?: throw OpexError.WithdrawNotFound.exception()

        if (!withdraw.canBeRejected()) throw OpexError.WithdrawCannotBeRejected.exception()

        val sourceWallet = walletManager.findWalletById(withdraw.wallet) ?: throw OpexError.WalletNotFound.exception()
        val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(
            sourceWallet.owner, WalletType.MAIN, sourceWallet.currency
        ) ?: walletManager.createWallet(
            sourceWallet.owner, Amount(sourceWallet.currency, BigDecimal.ZERO), sourceWallet.currency, WalletType.MAIN
        )
        val transferResultDetailed = transferManager.transfer(
            TransferCommand(
                sourceWallet,
                receiverWallet,
                Amount(sourceWallet.currency, withdraw.amount + withdraw.appliedFee),
                rejectCommand.statusReason,
                "wallet:withdraw:${withdraw.withdrawId}:${WithdrawStatus.REJECTED}:${LocalDateTime.now()}",
                TransferCategory.WITHDRAW_REJECT
            )
        )
        withdrawPersister.persist(
            Withdraw(
                withdraw.withdrawId,
                withdraw.ownerUuid,
                withdraw.currency,
                withdraw.wallet,
                withdraw.amount,
                withdraw.requestTransaction,
                transferResultDetailed.tx,
                withdraw.appliedFee,
                null,
                withdraw.destSymbol,
                withdraw.destAddress,
                withdraw.destNetwork,
                withdraw.destNote,
                null,
                rejectCommand.statusReason,
                WithdrawStatus.REJECTED,
                withdraw.applicator,
                withdraw.withdrawType,
                withdraw.attachment,
                withdraw.createDate,
                LocalDateTime.now(),
                withdraw.transferMethod,
                otpRequired = otpRequiredCount
            )
        )
        publishWithdrawEvent(withdraw, WithdrawStatus.REJECTED)

        return WithdrawActionResult(withdraw.withdrawId!!, WithdrawStatus.REJECTED)
    }

    suspend fun doneWithdraw(withdrawId: Long): WithdrawActionResult {
        val withdraw = withdrawPersister.findById(withdrawId) ?: throw OpexError.WithdrawNotFound.exception()

        if (!withdraw.canBeDone()) throw OpexError.WithdrawCannotBeDone.exception()

        withdraw.status = WithdrawStatus.DONE
        withdrawPersister.persist(withdraw)

        return WithdrawActionResult(withdrawId, WithdrawStatus.DONE)
    }

    suspend fun findWithdraw(id: Long): WithdrawResponse? {
        return withdrawPersister.findWithdrawResponseById(id)
    }

    suspend fun findByCriteria(
        ownerUuid: String?,
        currency: String?,
        destTxRef: String?,
        destAddress: String?,
        status: List<WithdrawStatus>,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        ascendingByTime: Boolean,
        offset: Int,
        size: Int,
    ): List<WithdrawAdminResponse> {
        return withdrawPersister.findByCriteria(
            ownerUuid, currency, destTxRef, destAddress, status, startTime, endTime, ascendingByTime, offset, size
        )
    }

    suspend fun findWithdrawHistory(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean? = false,
    ): List<WithdrawResponse> {
        return withdrawPersister.findWithdrawHistory(uuid, currency, startTime, endTime, limit, offset, ascendingByTime)
    }

    suspend fun findWithdrawHistoryCount(
        uuid: String,
        currency: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
    ): Long {
        return withdrawPersister.findWithdrawHistoryCount(uuid, currency, startTime, endTime)
    }

    suspend fun getWithdrawSummary(
        uuid: String,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): List<TransactionSummary> {
        return withdrawPersister.getWithdrawSummary(
            uuid,
            startTime,
            endTime,
            limit,
        )
    }

    private suspend fun verifyOwnershipForWithdraw(token: String, withdrawCommand: WithdrawCommand) {
        if (withdrawCommand.withdrawType != WithdrawType.OFF_CHAIN) return

        val transferMethod = withdrawCommand.transferMethod
        val destAddress = withdrawCommand.destAddress

        val verified = when (transferMethod) {
            TransferMethod.CARD -> profileProxy.verifyBankAccountOwnership(
                token = token,
                cardNumber = destAddress,
                iban = null
            )

            TransferMethod.SHEBA -> profileProxy.verifyBankAccountOwnership(
                token = token,
                cardNumber = null,
                iban = destAddress
            )

            else -> return
        }

        if (!verified) when (transferMethod) {
            TransferMethod.CARD -> throw OpexError.CardOwnershipMismatch.exception()
            TransferMethod.SHEBA -> throw OpexError.IbanOwnershipMismatch.exception()
            else -> return
        }
    }

}