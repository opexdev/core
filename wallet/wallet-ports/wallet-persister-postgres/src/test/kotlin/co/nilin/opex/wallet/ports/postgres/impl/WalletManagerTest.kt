package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import co.nilin.opex.wallet.ports.postgres.impl.sample.VALID
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletManagerTest {
    private val walletLimitsRepository: WalletLimitsRepository = mock()
    private val walletRepository: WalletRepository = mock()
    private val walletOwnerRepository: WalletOwnerRepository = mock()
    private val currencyRepository: CurrencyRepository = mock { }

    private var transactionRepository: TransactionRepository = mock {
        on {
            calculateWithdrawStatistics(
                eq(VALID.WALLET_OWNER.id!!),
                eq(VALID.WALLET.id!!),
                any(),
                any()
            )
        } doReturn Mono.empty()
        on {
            calculateDepositStatistics(
                eq(VALID.WALLET_OWNER.id!!),
                eq(VALID.WALLET.id!!),
                any(),
                any()
            )
        } doReturn Mono.empty()
    }

    private val walletManagerImpl: WalletManagerImpl = WalletManagerImpl(
        walletLimitsRepository, transactionRepository, walletRepository, walletOwnerRepository, currencyRepository
    )

    private fun stubNoWalletLimit(action: String) =
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.WALLET.id!!,
                    action
                )
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    action,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType(
                    VALID.USER_LEVEL_REGISTERED,
                    VALID.CURRENCY.symbol,
                    action,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
        }

    private fun stubAllWalletLimit(action: String) = stubbing(walletLimitsRepository) {
        on {
            findByOwnerAndCurrencyAndWalletAndAction(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol,
                VALID.WALLET.id!!,
                action
            )
        } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW.copy(action = action))
        on {
            findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW.copy(action = action))
        on {
            findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW.copy(action = action))
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
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenOwnerAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.WALLET.id!!,
                    VALID.ACTION_WITHDRAW
                )
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.owner!!,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.currency,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.action,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.walletType
                )
            } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
            on {
                findByLevelAndCurrencyAndActionAndWalletType(
                    VALID.USER_LEVEL_REGISTERED,
                    VALID.CURRENCY.symbol,
                    VALID.ACTION_WITHDRAW,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenOwnerAndWalletLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.owner!!,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.currency,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.walletId!!,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.action
                )
            } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.ACTION_WITHDRAW,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType(
                    VALID.USER_LEVEL_REGISTERED,
                    VALID.CURRENCY.symbol,
                    VALID.ACTION_WITHDRAW,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenLevelAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.WALLET.id!!,
                    VALID.ACTION_WITHDRAW
                )
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.ACTION_WITHDRAW,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType(
                    VALID.USER_LEVEL_REGISTERED,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.currency,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.action,
                    VALID.WALLET_LIMITS_MODEL_WITHDRAW.walletType
                )
            } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
        }

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
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.WALLET.id!!,
                    VALID.ACTION_DEPOSIT
                )
            } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_DEPOSIT)
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.ACTION_DEPOSIT,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType(
                    VALID.USER_LEVEL_REGISTERED,
                    VALID.CURRENCY.symbol,
                    VALID.ACTION_DEPOSIT,
                    VALID.WALLET_TYPE_MAIN
                )
            } doReturn Mono.empty()
        }

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.WALLET, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWallet_whenFindWalletByOwnerAndCurrencyAndType_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { findById(VALID.WALLET_OWNER.id!!) } doReturn Mono.just(VALID.WALLET_OWNER.toModel())
        }
        stubbing(walletRepository) {
            on {
                findByOwnerAndTypeAndCurrency(
                    VALID.WALLET_OWNER.id!!,
                    VALID.WALLET_TYPE_MAIN,
                    VALID.CURRENCY.symbol
                )
            } doReturn Mono.just(VALID.WALLET.toModel())
        }
        stubbing(currencyRepository) {
            on {
                findBySymbol(VALID.CURRENCY.symbol)
            } doReturn Mono.just(VALID.CURRENCY.toModel())
        }

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
        stubbing(walletRepository) {
            on {
                save(VALID.WALLET.copy(id = null).toModel())
            } doReturn Mono.just(VALID.WALLET.toModel())
        }

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
        stubbing(walletRepository) {
            on {
                updateBalance(eq(VALID.WALLET.id!!), eq(BigDecimal.valueOf(1)))
            } doReturn Mono.just(1)
        }

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
        stubbing(walletRepository) {
            on {
                updateBalance(any(), eq(BigDecimal.valueOf(1)))
            } doReturn Mono.just(0)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.increaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWrongAmount_whenIncreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(20), any())
            } doReturn Mono.just(0)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.increaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(-1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWallet_whenDecreaseBalance_thenSuccess(): Unit = runBlocking {
        stubbing(walletRepository) {
            on { updateBalance(eq(VALID.WALLET.id!!), eq(BigDecimal.valueOf(-1))) } doReturn Mono.just(1)
        }

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
        stubbing(walletRepository) {
            on {
                updateBalance(any(), eq(BigDecimal.valueOf(-1)))
            } doReturn Mono.just(0)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWrongAmount_whenDecreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(VALID.WALLET_OWNER.id!!), eq(BigDecimal.valueOf(-1)))
            } doReturn Mono.just(0)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.WALLET,
                    BigDecimal.valueOf(-1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWallet_whenFindWalletById_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletRepository) {
            on { findById(VALID.WALLET.id!!) } doReturn Mono.just(VALID.WALLET.toModel())
        }
        stubbing(walletOwnerRepository) {
            on {
                findById(VALID.WALLET_OWNER.id!!)
            } doReturn Mono.just(VALID.WALLET_OWNER.toModel())
        }
        stubbing(currencyRepository) {
            on {
                findById(VALID.CURRENCY.symbol)
            } doReturn Mono.just(VALID.CURRENCY.toModel())
        }

        val wallet = walletManagerImpl.findWalletById(VALID.WALLET.id!!)

        assertThat(wallet).isNotNull
        assertThat(wallet!!.id).isEqualTo(VALID.WALLET.id)
        assertThat(wallet.balance).isEqualTo(VALID.WALLET.balance)
        assertThat(wallet.currency.symbol).isEqualTo(VALID.CURRENCY.symbol)
    }
}
