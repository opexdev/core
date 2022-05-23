package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.service.sample.VALID
import co.nilin.opex.wallet.ports.postgres.dao.*
import co.nilin.opex.wallet.ports.postgres.dto.toModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletManagerTest {
    private val walletLimitsRepository: WalletLimitsRepository = mock()
    private val walletRepository: WalletRepository = mock()
    private val walletOwnerRepository: WalletOwnerRepository = mock()
    private val currencyRepository: CurrencyRepository = mock { }

    private var transactionRepository: TransactionRepository = mock {
        on { calculateWithdrawStatistics(eq(2), eq(20), any(), any()) } doReturn Mono.empty()
    }

    private val walletManagerImpl: WalletManagerImpl = WalletManagerImpl(
        walletLimitsRepository, transactionRepository, walletRepository, walletOwnerRepository, currencyRepository
    )

    private fun stubNoWalletLimit(action: String) =
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.SOURCE_WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.SOURCE_WALLET.id!!,
                    action
                )
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.SOURCE_WALLET_OWNER.id!!,
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
                VALID.SOURCE_WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol,
                VALID.SOURCE_WALLET.id!!,
                action
            )
        } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
        on {
            findByOwnerAndCurrencyAndActionAndWalletType(
                VALID.SOURCE_WALLET_OWNER.id!!,
                VALID.CURRENCY.symbol,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
        on {
            findByLevelAndCurrencyAndActionAndWalletType(
                VALID.USER_LEVEL_REGISTERED,
                VALID.CURRENCY.symbol,
                action,
                VALID.WALLET_TYPE_MAIN
            )
        } doReturn Mono.just(VALID.WALLET_LIMITS_MODEL_WITHDRAW)
    }

    @Test
    fun givenWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNoWallet_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_WITHDRAW)

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun giNoCurrency_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        stubbing(currencyRepository) {
            on { findBySymbol(VALID.CURRENCY.symbol) } doReturn Mono.empty()
            on { findById(VALID.CURRENCY.symbol) } doReturn Mono.empty()
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenEmptyWallet_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWrongAmount_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(-1))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenOwnerAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.SOURCE_WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.SOURCE_WALLET.id!!,
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

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(1))

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
                    VALID.SOURCE_WALLET_OWNER.id!!,
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

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenLevelAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(
                    VALID.SOURCE_WALLET_OWNER.id!!,
                    VALID.CURRENCY.symbol,
                    VALID.SOURCE_WALLET.id!!,
                    VALID.ACTION_WITHDRAW
                )
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(
                    VALID.SOURCE_WALLET_OWNER.id!!,
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

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenAllLimits_whenIsWithdrawAllowedWithValidAmount_thenReturnTrue(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenAllLimits_whenIsWithdrawAllowedWithInvalidAmount_thenReturnFalse(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_WITHDRAW)

        val isAllowed = walletManagerImpl.isWithdrawAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWalletWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNotExistWallet_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenNoCurrency_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        stubbing(currencyRepository) {
            on { findBySymbol(anyString()) } doReturn Mono.empty()
            on { findById(anyString()) } doReturn Mono.empty()
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isDepositAllowed(
                    VALID.SOURCE_WALLET,
                    BigDecimal.valueOf(0.5)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenEmptyWallet_whenIsDepositAllowed_thenFalse(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = runBlocking { walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5)) }

        verify(walletLimitsRepository, never()).findByOwnerAndCurrencyAndWalletAndAction(
            VALID.SOURCE_WALLET_OWNER.id!!,
            VALID.CURRENCY.symbol,
            VALID.SOURCE_WALLET.id!!,
            VALID.ACTION_DEPOSIT
        )
        verify(walletLimitsRepository, never()).findByOwnerAndCurrencyAndActionAndWalletType(
            VALID.SOURCE_WALLET_OWNER.id!!,
            VALID.CURRENCY.symbol,
            VALID.ACTION_DEPOSIT,
            VALID.WALLET_TYPE_MAIN
        )
        verify(walletLimitsRepository, never()).findByLevelAndCurrencyAndActionAndWalletType(
            VALID.USER_LEVEL_REGISTERED,
            VALID.CURRENCY.symbol,
            VALID.ACTION_DEPOSIT,
            VALID.WALLET_TYPE_MAIN
        )
        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWrongAmount_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(-1))
            }
        }
    }

    @Test
    fun givenAllLimits_whenIsDepositAllowedWithValidAmount_thenReturnTrue(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenWalletWithWalletLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        stubAllWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        stubNoWalletLimit(VALID.ACTION_DEPOSIT)

        val isAllowed = walletManagerImpl.isDepositAllowed(VALID.SOURCE_WALLET, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWallet_whenFindWalletByOwnerAndCurrencyAndType_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { findById(VALID.SOURCE_WALLET_OWNER.id!!) } doReturn Mono.just(VALID.SOURCE_WALLET_OWNER.toModel())
        }
        stubbing(walletRepository) {
            on {
                findByOwnerAndTypeAndCurrency(
                    VALID.SOURCE_WALLET_OWNER.id!!,
                    VALID.WALLET_TYPE_MAIN,
                    VALID.CURRENCY.symbol
                )
            } doReturn Mono.just(VALID.SOURCE_WALLET.toModel())
        }
        stubbing(currencyRepository) {
            on {
                findBySymbol(VALID.CURRENCY.symbol)
            } doReturn Mono.just(VALID.CURRENCY.toModel())
        }

        val wallet = walletManagerImpl.findWalletByOwnerAndCurrencyAndType(
            VALID.SOURCE_WALLET_OWNER,
            VALID.WALLET_TYPE_MAIN,
            VALID.CURRENCY
        )

        assertThat(wallet).isNotNull
        assertThat(wallet!!.owner.id).isEqualTo(VALID.SOURCE_WALLET_OWNER.id)
        assertThat(wallet.currency.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(wallet.type).isEqualTo(VALID.WALLET_TYPE_MAIN)
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenCreateWallet_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                save(VALID.SOURCE_WALLET.toModel())
            } doReturn Mono.just(VALID.SOURCE_WALLET.toModel())
        }

        val wallet = walletManagerImpl.createWallet(
            VALID.SOURCE_WALLET_OWNER,
            VALID.SOURCE_WALLET.balance,
            VALID.SOURCE_WALLET.currency,
            VALID.SOURCE_WALLET.type
        )

        assertThat(wallet).isNotNull
        assertThat(wallet.owner.id).isEqualTo(VALID.SOURCE_WALLET_OWNER.id)
        assertThat(wallet.currency.symbol).isEqualTo(VALID.CURRENCY.symbol)
        assertThat(wallet.type).isEqualTo(VALID.WALLET_TYPE_MAIN)
    }

    @Test
    fun givenWallet_whenIncreaseBalance_thenSuccess(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(20), any())
            } doReturn Mono.just(1)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.increaseBalance(
                    VALID.SOURCE_WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.doesNotThrowAnyException()
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
                    VALID.SOURCE_WALLET,
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
                    VALID.SOURCE_WALLET,
                    BigDecimal.valueOf(-1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWallet_whenDecreaseBalance_thenSuccess(): Unit = runBlocking {
        stubbing(walletRepository) {
            on { updateBalance(eq(VALID.SOURCE_WALLET.id!!), eq(BigDecimal.valueOf(-1))) } doReturn Mono.just(1)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.SOURCE_WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.doesNotThrowAnyException()
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
                    VALID.SOURCE_WALLET,
                    BigDecimal.valueOf(1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWrongAmount_whenDecreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(VALID.SOURCE_WALLET_OWNER.id!!), eq(BigDecimal.valueOf(-1)))
            } doReturn Mono.just(0)
        }

        assertThatThrownBy {
            runBlocking {
                walletManagerImpl.decreaseBalance(
                    VALID.SOURCE_WALLET,
                    BigDecimal.valueOf(-1)
                )
            }
        }.isNotInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun givenWallet_whenFindWalletById_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletRepository) {
            on { findById(VALID.SOURCE_WALLET.id!!) } doReturn Mono.just(VALID.SOURCE_WALLET.toModel())
        }
        stubbing(walletOwnerRepository) {
            on {
                findById(VALID.SOURCE_WALLET_OWNER.id!!)
            } doReturn Mono.just(VALID.SOURCE_WALLET_OWNER.toModel())
        }
        stubbing(currencyRepository) {
            on {
                findById(VALID.CURRENCY.symbol)
            } doReturn Mono.just(VALID.CURRENCY.toModel())
        }

        val wallet = walletManagerImpl.findWalletById(20)

        assertThat(wallet).isNotNull
        assertThat(wallet!!.id).isEqualTo(20)
        assertThat(wallet.balance).isEqualTo(Amount(VALID.CURRENCY, BigDecimal.valueOf(0.5)))
        assertThat(wallet.currency.symbol).isEqualTo("ETH")
    }
}
