package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.ports.postgres.model.UserLimitsModel
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
    fun givenOwnerWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "withdraw") } doReturn flow { }
            on { findByLevelAndAction(eq("1"), eq("withdraw")) } doReturn flow {}
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed = walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(0.5)))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenWrongAmount_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "withdraw") } doReturn flow { }
            on { findByLevelAndAction(eq("1"), eq("withdraw")) } doReturn flow {}
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed = walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(-5)))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenOwnerWithLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "withdraw") } doReturn flow {
                emit(
                    UserLimitsModel(
                        1,
                        null,
                        walletOwner.id()!!,
                        "withdraw",
                        "main",
                        BigDecimal.valueOf(100),
                        10,
                        BigDecimal.valueOf(3000),
                        300
                    )
                )
            }
            on { findByLevelAndAction(eq("1"), eq("withdraw")) } doReturn flow { }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(120)))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenLevelWithLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "withdraw") } doReturn flow { }
            on { findByLevelAndAction(eq("1"), eq("withdraw")) } doReturn flow {
                emit(
                    UserLimitsModel(
                        1,
                        "1",
                        null,
                        "withdraw",
                        "main",
                        BigDecimal.valueOf(100),
                        10,
                        BigDecimal.valueOf(3000),
                        300
                    )
                )
            }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(120)))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenOwnerWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "deposit") } doReturn flow { }
            on { findByLevelAndAction(eq("1"), eq("deposit")) } doReturn flow {}
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed = walletOwnerManagerImpl.isDepositAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(0.5)))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenWrongAmount_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "deposit") } doReturn flow { }
            on { findByLevelAndAction(eq("1"), eq("deposit")) } doReturn flow {}
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed = walletOwnerManagerImpl.isDepositAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(-5)))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenOwnerWithLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(walletOwner.id()!!, "deposit") } doReturn flow {
                emit(
                    UserLimitsModel(
                        1,
                        null,
                        walletOwner.id()!!,
                        "deposit",
                        "main",
                        BigDecimal.valueOf(100),
                        10,
                        BigDecimal.valueOf(3000),
                        300
                    )
                )
            }
            on { findByLevelAndAction(eq("1"), eq("deposit")) } doReturn flow { }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }
        val isAllowed =
            walletOwnerManagerImpl.isDepositAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(120)))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenLevelWithLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            stubbing(userLimitsRepository) {
                on { findByOwnerAndAction(walletOwner.id()!!, "deposit") } doReturn flow { }
                on { findByLevelAndAction(eq("1"), eq("deposit")) } doReturn flow {
                    emit(
                        UserLimitsModel(
                            1,
                            "1",
                            null,
                            "deposit",
                            "main",
                            BigDecimal.valueOf(100),
                            10,
                            BigDecimal.valueOf(3000),
                            300
                        )
                    )
                }
            }
            stubbing(walletConfigRepository) {
                on { findAll() } doReturn Flux.just(WalletConfigModel("default", "ETH"))
            }
            stubbing(transactionRepository) {
                on {
                    calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
                } doReturn Mono.empty()
            }
            val isAllowed =
                walletOwnerManagerImpl.isDepositAllowed(walletOwner, Amount(currency, BigDecimal.valueOf(120)))

            assertThat(isAllowed).isFalse()
        }
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
