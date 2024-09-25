package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.WalletLimitAction
import co.nilin.opex.wallet.ports.postgres.dao.TransactionRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletConfigRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletLimitsRepository
import co.nilin.opex.wallet.ports.postgres.dao.WalletOwnerRepository
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import co.nilin.opex.wallet.ports.postgres.model.WalletConfigModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Suppress("ReactiveStreamsUnusedPublisher")
private class WalletOwnerManagerTest {

    private val walletLimitsRepository: WalletLimitsRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val walletOwnerRepository: WalletOwnerRepository = mockk()
    private val walletConfigRepository: WalletConfigRepository = mockk()
    private val walletOwnerManagerImpl: WalletOwnerManagerImpl = WalletOwnerManagerImpl(
        walletLimitsRepository, transactionRepository, walletConfigRepository, walletOwnerRepository
    )

    private fun stubNoUserLimit(action: WalletLimitAction) {
        every { walletLimitsRepository.findByOwnerAndAction(VALID.WALLET_OWNER.id!!, action) } returns flow { }
        every {
            walletLimitsRepository.findByLevelAndAction(
                eq(VALID.USER_LEVEL_REGISTERED),
                eq(action)
            )
        } returns flow {}
    }

    @Test
    fun givenOwnerWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoUserLimit(WalletLimitAction.WITHDRAW)
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

        val isAllowed = walletOwnerManagerImpl.isWithdrawAllowed(
            VALID.WALLET_OWNER,
            Amount(VALID.CURRENCY, BigDecimal.valueOf(0.5))
        )

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNoLimit_whenIsWithdrawAllowedByCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        stubNoUserLimit(WalletLimitAction.WITHDRAW)
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

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
        every {
            walletLimitsRepository.findByOwnerAndAction(
                VALID.WALLET_OWNER.id!!,
                WalletLimitAction.WITHDRAW
            )
        } returns flow {
            emit(VALID.USER_LIMITS_MODEL_WITHDRAW)
        }
        every { walletLimitsRepository.findByLevelAndAction(eq("1"), eq(WalletLimitAction.WITHDRAW)) } returns flow { }
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenLevelWithLimit_whenIsWithdrawAllowedCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndAction(
                VALID.WALLET_OWNER.id!!,
                WalletLimitAction.WITHDRAW
            )
        } returns flow { }
        every {
            walletLimitsRepository.findByLevelAndAction(
                eq(VALID.USER_LEVEL_REGISTERED),
                eq(WalletLimitAction.WITHDRAW)
            )
        } returns flow {
            emit(VALID.USER_LIMITS_MODEL_WITHDRAW)
        }
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateWithdrawStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

        val isAllowed =
            walletOwnerManagerImpl.isWithdrawAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenOwnerWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoUserLimit(WalletLimitAction.DEPOSIT)
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

        val isAllowed = walletOwnerManagerImpl.isDepositAllowed(
            VALID.WALLET_OWNER,
            Amount(VALID.CURRENCY, BigDecimal.valueOf(0.5))
        )

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNoUserLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoUserLimit(WalletLimitAction.DEPOSIT)
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

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
        every {
            walletLimitsRepository.findByOwnerAndAction(
                VALID.WALLET_OWNER.id!!,
                WalletLimitAction.DEPOSIT
            )
        } returns flow {
            emit(VALID.USER_LIMITS_MODEL_DEPOSIT)
        }
        every {
            walletLimitsRepository.findByLevelAndAction(
                eq(VALID.USER_LEVEL_REGISTERED),
                eq(WalletLimitAction.DEPOSIT)
            )
        } returns flow { }
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

        val isAllowed =
            walletOwnerManagerImpl.isDepositAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenLevelWithLimit_whenIsDepositAllowedCrossedAmount_thenReturnFalse(): Unit = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndAction(
                VALID.WALLET_OWNER.id!!,
                WalletLimitAction.DEPOSIT
            )
        } returns flow { }
        every {
            walletLimitsRepository.findByLevelAndAction(
                eq(VALID.USER_LEVEL_REGISTERED),
                eq(WalletLimitAction.DEPOSIT)
            )
        } returns flow {
            emit(VALID.USER_LIMITS_MODEL_DEPOSIT)
        }
        every { walletConfigRepository.findAll() } returns Flux.just(
            WalletConfigModel("default", VALID.CURRENCY.symbol)
        )
        every {
            transactionRepository.calculateDepositStatisticsBasedOnCurrency(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Mono.empty()

        val isAllowed =
            walletOwnerManagerImpl.isDepositAllowed(
                VALID.WALLET_OWNER,
                Amount(VALID.CURRENCY, BigDecimal.valueOf(120))
            )

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWalletOwner_whenFindWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        every { walletOwnerRepository.findByUuid(VALID.WALLET_OWNER.uuid) } returns Mono.just(VALID.WALLET_OWNER.toModel())

        val wo = walletOwnerManagerImpl.findWalletOwner(VALID.WALLET_OWNER.uuid)

        assertThat(wo!!.id).isEqualTo(VALID.WALLET_OWNER.id)
        assertThat(wo.uuid).isEqualTo(VALID.WALLET_OWNER.uuid)
    }

    @Test
    fun givenWalletOwner_whenCreateWalletOwner_thenReturnWalletOwner(): Unit = runBlocking {
        every { walletOwnerRepository.save(any()) } returns Mono.just(VALID.WALLET_OWNER.toModel())

        val wo = walletOwnerManagerImpl.createWalletOwner(
            VALID.WALLET_OWNER.uuid,
            VALID.WALLET_OWNER.title,
            VALID.WALLET_OWNER.level
        )

        assertThat(wo.id).isEqualTo(VALID.WALLET_OWNER.id)
        assertThat(wo.uuid).isEqualTo(VALID.WALLET_OWNER.uuid)
    }
}
