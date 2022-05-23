package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.dao.UserLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletConfigRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import co.nilin.opex.wallet.ports.postgres.model.WalletConfigModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletOwnerManagerTest {
    private val userLimitsRepository: UserLimitsRepository = mock()
    private val transactionRepository: TransactionRepository = mock()
    private val walletOwnerRepository: WalletOwnerRepository = mock()
    private val walletConfigRepository: WalletConfigRepository = mock()
    private val walletOwnerManagerImpl: WalletOwnerManagerImpl = WalletOwnerManagerImpl(
        userLimitsRepository, transactionRepository, walletConfigRepository, walletOwnerRepository
    )

    private fun stubNoUserLimit(action: String) = stubbing(userLimitsRepository) {
        on { findByOwnerAndAction(VALID.WALLET_OWNER.id!!, action) } doReturn flow { }
        on { findByLevelAndAction(eq(VALID.USER_LEVEL_REGISTERED), eq(action)) } doReturn flow {}
    }

    @Test
    fun givenOwnerWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoUserLimit(VALID.ACTION_WITHDRAW)
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        val isAllowed = walletOwnerManagerImpl.isWithdrawAllowed(
            VALID.WALLET_OWNER,
            Amount(VALID.CURRENCY, BigDecimal.valueOf(0.5))
        )

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNoLimit_whenIsWithdrawAllowedByCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        stubNoUserLimit(VALID.ACTION_WITHDRAW)
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        assertThatThrownBy {
            runBlocking {
                walletOwnerManagerImpl.isWithdrawAllowed(
                    VALID.WALLET_OWNER,
                    Amount(VALID.CURRENCY, BigDecimal.valueOf(-5))
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenOwnerWithLimit_whenIsWithdrawAllowedWithCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(VALID.WALLET_OWNER.id!!, "withdraw") } doReturn flow {
                emit(VALID.USER_LIMITS_MODEL_WITHDRAW)
            }
            on { findByLevelAndAction(eq("1"), eq("withdraw")) } doReturn flow { }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenLevelWithLimit_whenIsWithdrawAllowedCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(VALID.WALLET_OWNER.id!!, VALID.ACTION_WITHDRAW) } doReturn flow { }
            on { findByLevelAndAction(eq(VALID.USER_LEVEL_REGISTERED), eq(VALID.ACTION_WITHDRAW)) } doReturn flow {
                emit(VALID.USER_LIMITS_MODEL_WITHDRAW)
            }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateWithdrawStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenOwnerWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoUserLimit(VALID.ACTION_DEPOSIT)
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        val isAllowed = walletOwnerManagerImpl.isDepositAllowed(
            VALID.WALLET_OWNER,
            Amount(VALID.CURRENCY, BigDecimal.valueOf(0.5))
        )

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNoUserLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoUserLimit(VALID.ACTION_DEPOSIT)
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        assertThatThrownBy {
            runBlocking {
                walletOwnerManagerImpl.isDepositAllowed(
                    VALID.WALLET_OWNER,
                    Amount(VALID.CURRENCY, BigDecimal.valueOf(-5))
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenOwnerWithLimit_whenIsDepositAllowedCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(VALID.WALLET_OWNER.id!!, VALID.ACTION_DEPOSIT) } doReturn flow {
                emit(VALID.USER_LIMITS_MODEL_DEPOSIT)
            }
            on { findByLevelAndAction(eq(VALID.USER_LEVEL_REGISTERED), eq(VALID.ACTION_DEPOSIT)) } doReturn flow { }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        val isAllowed =
            walletOwnerManagerImpl.isDepositAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenLevelWithLimit_whenIsDepositAllowedCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        stubbing(userLimitsRepository) {
            on { findByOwnerAndAction(VALID.WALLET_OWNER.id!!, VALID.ACTION_DEPOSIT) } doReturn flow { }
            on { findByLevelAndAction(eq(VALID.USER_LEVEL_REGISTERED), eq(VALID.ACTION_DEPOSIT)) } doReturn flow {
                emit(VALID.USER_LIMITS_MODEL_DEPOSIT)
            }
        }
        stubbing(walletConfigRepository) {
            on { findAll() } doReturn Flux.just(WalletConfigModel("default", VALID.CURRENCY.symbol))
        }
        stubbing(transactionRepository) {
            on {
                calculateDepositStatisticsBasedOnCurrency(anyLong(), anyString(), any(), any(), anyString())
            } doReturn Mono.empty()
        }

        val isAllowed =
            walletOwnerManagerImpl.isDepositAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWalletOwner_whenFindWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { findByUuid(VALID.WALLET_OWNER.uuid) } doReturn Mono.just(VALID.WALLET_OWNER.toModel())
        }

        val wo = walletOwnerManagerImpl.findWalletOwner(VALID.WALLET_OWNER.uuid)

        assertThat(wo!!.id).isEqualTo(VALID.WALLET_OWNER.id)
        assertThat(wo.uuid).isEqualTo(VALID.WALLET_OWNER.uuid)
    }

    @Test
    fun givenWalletOwner_whenCreateWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { save(any()) } doReturn Mono.just(VALID.WALLET_OWNER.toModel())
        }

        val wo = walletOwnerManagerImpl.createWalletOwner(
            VALID.WALLET_OWNER.uuid,
            VALID.WALLET_OWNER.title,
            VALID.WALLET_OWNER.level
        )

        assertThat(wo.id).isEqualTo(VALID.WALLET_OWNER.id)
        assertThat(wo.uuid).isEqualTo(VALID.WALLET_OWNER.uuid)
    }
}
