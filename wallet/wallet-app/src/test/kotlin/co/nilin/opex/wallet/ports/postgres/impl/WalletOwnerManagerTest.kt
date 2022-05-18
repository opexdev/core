package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private class WalletOwnerManagerTest : WalletOwnerManagerTestBase() {
    @Test
    fun givenFullWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        val isAllowed = walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(0.5)))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(12)))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenUUID_whenFindWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        val wo = walletOwnerManagerImpl.findWalletOwner(walletOwner.uuid())

        assertThat(wo!!.id()).isEqualTo(walletOwner.id())
        assertThat(wo.uuid()).isEqualTo(walletOwner.uuid())
    }

    @Test
    fun givenOwnerInfo_whenCreateWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        val wo = walletOwnerManagerImpl.createWalletOwner(walletOwner.uuid(), walletOwner.title(), walletOwner.level())

        assertThat(wo.id()).isEqualTo(walletOwner.id())
        assertThat(wo.uuid()).isEqualTo(walletOwner.uuid())
    }
}
