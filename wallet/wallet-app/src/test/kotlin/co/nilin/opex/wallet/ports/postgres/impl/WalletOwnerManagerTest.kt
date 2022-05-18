package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.ports.postgres.model.WalletConfigModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletOwnerManagerTest : WalletOwnerManagerTestBase() {
    @Test
    fun givenFullWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(anyLong(), eq("withdraw")) } doReturn flow { }
        }
        val isAllowed = walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(0.5)))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "withdraw") } doReturn flow { }
            on { findByLevelAndAction(eq("1"), eq("withdraw")) } doReturn flow {}
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(
                    anyLong(),
                    anyString(),
                    any(),
                    any(),
                    anyString()
                )
            } doReturn Mono.empty()
            on {
                calculateWithdrawStatisticsBasedOnCurrency(
                    anyLong(),
                    anyString(),
                    any(),
                    any(),
                    anyString()
                )
            } doReturn Mono.empty()
        }
        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(12)))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenUUID_whenFindWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { findByUuid(walletOwner.uuid()) } doReturn Mono.just(
                WalletOwnerModel(
                    walletOwner.id(),
                    walletOwner.uuid(),
                    walletOwner.title(),
                    walletOwner.level(),
                    walletOwner.isTradeAllowed(),
                    walletOwner.isWithdrawAllowed(),
                    walletOwner.isDepositAllowed()
                )
            )
        }
        val wo = walletOwnerManagerImpl.findWalletOwner(walletOwner.uuid())

        assertThat(wo!!.id()).isEqualTo(walletOwner.id())
        assertThat(wo.uuid()).isEqualTo(walletOwner.uuid())
    }

    @Test
    fun givenOwnerInfo_whenCreateWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { save(any()) } doReturn Mono.just(
                WalletOwnerModel(
                    walletOwner.id(),
                    walletOwner.uuid(),
                    walletOwner.title(),
                    walletOwner.level(),
                    walletOwner.isTradeAllowed(),
                    walletOwner.isWithdrawAllowed(),
                    walletOwner.isDepositAllowed()
                )
            )
        }
        val wo =
            walletOwnerManagerImpl.createWalletOwner(walletOwner.uuid(), walletOwner.title(), walletOwner.level())

        assertThat(wo.id()).isEqualTo(walletOwner.id())
        assertThat(wo.uuid()).isEqualTo(walletOwner.uuid())
    }
}
