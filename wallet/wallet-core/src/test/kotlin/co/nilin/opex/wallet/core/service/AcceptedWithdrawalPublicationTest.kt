package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.*
import co.nilin.opex.wallet.core.spi.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class AcceptedWithdrawalPublicationTest {
    @Test
    fun `acceptance publishes pending lifecycle with request reference but no settlement`() = runBlocking<Unit> {
        val persister = mockk<WithdrawPersister>()
        val transfers = mockk<TransferManager>(relaxed = true)
        val events = mockk<WithdrawRequestEventSubmitter>(relaxed = true)
        val withdraw = Withdraw(withdrawId = 1L, withdrawUuid = "withdraw-1", ownerUuid = "customer",
            currency = "USDT", wallet = 10L, amount = BigDecimal("100"), appliedFee = BigDecimal("1"),
            requestTransaction = "request-tx", status = WithdrawStatus.CREATED,
            withdrawType = WithdrawType.ON_CHAIN, createDate = LocalDateTime.parse("2026-11-16T10:00:00"))
        coEvery { persister.findByWithdrawUuid("withdraw-1") } returns withdraw
        coEvery { persister.persist(any()) } answers { firstArg() }
        val service = WithdrawService(
            withdrawPersister = persister, walletManager = mockk(relaxed = true),
            walletOwnerManager = mockk(relaxed = true), currencyService = mockk(relaxed = true),
            transferManager = transfers, meterRegistry = mockk(relaxed = true),
            gatewayService = mockk(relaxed = true), precisionService = mockk(relaxed = true),
            accountantProxy = mockk(relaxed = true), withdrawRequestEventSubmitter = events,
            otpProxy = mockk(relaxed = true), profileProxy = mockk(relaxed = true),
            withdrawOtpPersister = mockk(relaxed = true), bcGatewayProxy = mockk(relaxed = true),
            systemUuid = "system", withdrawLimitEnabled = false, otpRequiredCount = 0,
            bankAccountValidation = false
        )
        assertThat(service.acceptWithdraw("withdraw-1", "admin").status).isEqualTo(WithdrawStatus.ACCEPTED)
        assertThat(withdraw.lastUpdateDate).isAfter(withdraw.createDate)
        coVerify(exactly = 1) { events.send(
            uuid = "customer", withdrawId = 1L, withdrawUuid = "withdraw-1",
            lifecycleEventId = "withdraw-lifecycle:withdraw-1:ACCEPTED",
            walletTransactionRef = "request-tx", businessReference = "wallet:withdraw:withdraw-1:ACCEPTED",
            currency = "USDT", amount = BigDecimal("101"), principalAmount = BigDecimal("100"),
            appliedCustomerFee = BigDecimal("1"), feeAsset = "USDT", transferCategory = null,
            withdrawStatus = WithdrawStatus.ACCEPTED, createDate = withdraw.createDate,
            lifecycleTransitionDate = withdraw.lastUpdateDate, completionDate = null,
            counterpartyReferenceId = null
        ) }
        coVerify(exactly = 0) { transfers.transfer(any()) }
    }
}
