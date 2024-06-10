package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import co.nilin.opex.wallet.ports.postgres.util.toModel
import io.mockk.MockKException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletManagerTest {
    private val walletLimitsRepository: WalletLimitsRepository = mockk()
    private val walletRepository: WalletRepository = mockk()
    private val walletOwnerRepository: WalletOwnerRepository = mockk()
    private val currencyRepository: CurrencyRepositoryV2 = mockk()

    private var transactionRepository: TransactionRepository = mockk {
        every {
            calculateWithdrawStatistics(
                eq(VALID.WALLET_OWNER.id!!),
                eq(VALID.WALLET.id!!),
                any(),
                any()
            )
        } returns Mono.empty()
        every {
            calculateDepositStatistics(
                eq(VALID.WALLET_OWNER.id!!),
                eq(VALID.WALLET.id!!),
                any(),
                any()
            )
        } returns Mono.empty()
    }

    private val walletManagerImpl: WalletManagerImplV2 = WalletManagerImplV2(
        walletLimitsRepository, transactionRepository, walletRepository, walletOwnerRepository, currencyRepository
    )

    private fun stubNoWalletLimit(action: String) = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.WALLET.id!!,
                action
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol!!,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()
    }

    private fun stubAllWalletLimit(action: String) = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.WALLET.id!!,
                action
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW.copy(action = action))
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW.copy(action = action))
        every {
            walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol!!,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW.copy(action = action))
    }

    @Test
    fun givenWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenEmptyWallet_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWrongAmount_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(-1))
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenOwnerAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.WALLET.id!!,
                VALID.ACTION_WITHDRAW
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.owner!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.currency!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.action!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.walletType
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
        every {
            walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol,
                VALID.ACTION_WITHDRAW,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenOwnerAndWalletLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.owner!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.currency!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.walletId!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.action!!
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.ACTION_WITHDRAW,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol!!,
                VALID.ACTION_WITHDRAW,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenLevelAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.WALLET.id!!,
                VALID.ACTION_WITHDRAW
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.ACTION_WITHDRAW,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.currency!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.action!!,
                VALID.WALLET_LIMITS_MODEL_WITHDRAW.walletType
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenAllLimits_whenIsWithdrawAllowedWithValidAmount_thenReturnTrue(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenAllLimits_whenIsWithdrawAllowedWithInvalidAmount_thenReturnFalse(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWalletWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenWrongAmount_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isDepositAllowed(VALID.WALLET, BigDecimal.valueOf(-1))
            }
        }
    }

    @Test
    fun givenAllLimits_whenIsDepositAllowedWithValidAmount_thenReturnTrue(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenWalletWithWalletLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.WALLET.id!!,
                VALID.ACTION_DEPOSIT
            )
        } returns Mono.just(VALID.WALLET_LIMITS_MODEL_DEPOSIT)
        every {
            walletLimitsRepository.findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol!!,
                VALID.ACTION_DEPOSIT,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()
        every {
            walletLimitsRepository.findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol!!,
                VALID.ACTION_DEPOSIT,
                VALID.WALLET_TYPE_MAIN
            )
        } returns Mono.empty()

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.WALLET, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWallet_whenFindWalletByOwnerAndCurrencyAndType_thenReturnWallet(): Unit = runBlocking {
        every { walletOwnerRepository.findById(VALID.WALLET_OWNER.id!!) } returns Mono.just(VALID.WALLET_OWNER.toModel())
        every {
            walletRepository.findByOwnerAndTypeAndCurrency(
                VALID.WALLET_OWNER.id!!,
                VALID.WALLET_TYPE_MAIN,
                VALID.CURRENCY.symbol!!
            )
        } returns Mono.just(VALID.WALLET.toModel())
        every {
            currencyRepository.fetchCurrency(symbol = VALID.CURRENCY.symbol)
        } returns Mono.just(VALID.CURRENCY.toModel())

        val wallet = walletManagerImpl.findWalletByOwnerAndCurrencyAndType(
            VALID.WALLET_OWNER,
            VALID.WALLET_TYPE_MAIN,
            VALID.CURRENCY
        )

        assertThat(wallet).isNotNull
        assertThat(wallet!!.owner.id).isEqualTo(VALID.WALLET_OWNER.id)
        assertThat(wallet.currency.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(wallet.type).isEqualTo(VALID.WALLET_TYPE_MAIN)
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenCreateWallet_thenReturnWallet(): Unit = runBlocking {
        every {
            walletRepository.save(VALID.WALLET.copy(id = null).toModel())
        } returns Mono.just(VALID.WALLET.toModel())

        val wallet = walletManagerImpl.createWallet(
            VALID.WALLET_OWNER,
            VALID.WALLET.balance,
            VALID.WALLET.currency,
            VALID.WALLET.type
        )

        assertThat(wallet).isNotNull
        assertThat(wallet.owner.id).isEqualTo(VALID.WALLET_OWNER.id)
        assertThat(wallet.currency.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(wallet.type).isEqualTo(VALID.WALLET_TYPE_MAIN)
    }

    @Test
    fun givenWallet_whenIncreaseBalance_thenSuccess(): Unit = runBlocking {
        every {
            walletRepository.updateBalance(eq(VALID.WALLET.id!!), eq(BigDecimal.valueOf(1)))
        } returns Mono.just(1)

        assertThatNoException().isThrownBy {
            runBlocking {
                walletManagerImpl.increaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }
    }

    @Test
    fun givenNoWallet_whenIncreaseBalance_thenThrow(): Unit = runBlocking {
        every {
            walletRepository.updateBalance(any(), eq(BigDecimal.valueOf(1)))
        } returns Mono.just(0)

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.increaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWrongAmount_whenIncreaseBalance_thenThrow(): Unit = runBlocking {
        every {
            walletRepository.updateBalance(eq(20), any(), any())
        } returns Mono.just(0)

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.increaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(-1)
                )
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWallet_whenDecreaseBalance_thenSuccess(): Unit = runBlocking {
        every {
            walletRepository.updateBalance(eq(VALID.WALLET.id!!), eq(BigDecimal.valueOf(-1)), any())
        } returns Mono.just(1)

        assertThatNoException().isThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }
    }

    @Test
    fun givenNoWallet_whenDecreaseBalance_thenThrow(): Unit = runBlocking {
        every {
            walletRepository.updateBalance(any(), eq(BigDecimal.valueOf(-1)), any())
        } returns Mono.just(0)

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWrongAmount_whenDecreaseBalance_thenThrow(): Unit = runBlocking {
        every {
            walletRepository.updateBalance(eq(VALID.WALLET_OWNER.id!!), eq(BigDecimal.valueOf(-1)), any())
        } returns Mono.just(0)

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(-1)
                )
            }
        }.isNotInstanceOf(MockKException::class.java)
    }

    @Test
    fun givenWallet_whenFindWalletById_thenReturnWallet(): Unit = runBlocking {
        every { walletRepository.findById(VALID.WALLET.id!!) } returns Mono.just(VALID.WALLET.toModel())
        every {
            walletOwnerRepository.findById(VALID.WALLET_OWNER.id!!)
        } returns Mono.just(VALID.WALLET_OWNER.toModel())
        every {
            currencyRepository.fetchCurrency(symbol = VALID.CURRENCY.symbol)
        } returns Mono.just(VALID.CURRENCY.toModel())

        val wallet = walletManagerImpl.findWalletById(VALID.WALLET.id!!)

        assertThat(wallet).isNotNull
        assertThat(wallet!!.id).isEqualTo(VALID.WALLET.id)
        assertThat(wallet.balance).isEqualTo(VALID.WALLET.balance)
        assertThat(wallet.currency.symbol).isEqualTo(VALID.CURRENCY.symbol)
    }
}
