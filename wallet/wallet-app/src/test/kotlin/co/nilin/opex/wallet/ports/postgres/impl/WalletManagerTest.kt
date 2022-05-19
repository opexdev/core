package co.nilin.opex.wallet.ports.postgres.impl

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import co.nilin.opex.wallet.ports.postgres.model.WalletLimitsModel
import co.nilin.opex.wallet.ports.postgres.model.WalletModel
import co.nilin.opex.wallet.ports.postgres.model.WalletOwnerModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import java.math.BigDecimal

private class WalletManagerTest : WalletManagerTestBase() {
    @Test
    fun givenFullWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(walletOwner.id()!!, "ETH", 20, "withdraw")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(walletOwner.id()!!, "ETH", "withdraw", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0.5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNotExistWallet_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 40L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5)) } }
    }

    @Test
    fun givenWrongCurrency_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = object : Currency {
                override fun getSymbol() = "WRONG"
                override fun getName() = "WRONG"
                override fun getPrecision() = 0.001
            }

            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5)) } }
    }

    @Test
    fun givenInsufficientAmount_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5)) }

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWrongAmount_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(-1)) } }
    }

    @Test
    fun givenOwnerAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "withdraw")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "withdraw", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenOwnerAndWalletLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "withdraw")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "withdraw", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenLevelAndWalletTypeLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "withdraw")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "withdraw", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    "1",
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenAllUnreachedLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "withdraw")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "withdraw", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    "1",
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenAllLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "withdraw")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "withdraw", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    "1",
                    2,
                    "withdraw",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(500))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(walletOwner.id()!!, "ETH", 20, "withdraw")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(walletOwner.id()!!, "ETH", "withdraw", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenFullWalletWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(walletOwner.id()!!, "ETH", 20, "deposit")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(walletOwner.id()!!, "ETH", "deposit", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "deposit", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0.5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenNotExistWallet_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 40L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(0.5)) } }
    }

    @Test
    fun givenWrongCurrency_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = object : Currency {
                override fun getSymbol() = "WRONG"
                override fun getName() = "WRONG"
                override fun getPrecision() = 0.001
            }

            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(0.5)) } }
    }

    @Test
    fun givenInsufficientAmount_whenIsDepositAllowed_thenFalse(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(walletOwner.id()!!, "ETH", 20, "withdraw")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(walletOwner.id()!!, "ETH", "withdraw", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "withdraw", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }
        val isAllowed = runBlocking { walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(0.5)) }

        verify(walletLimitsRepository, never()).findByOwnerAndCurrencyAndWalletAndAction(
            walletOwner.id()!!,
            "ETH",
            20,
            "withdraw"
        )
        verify(walletLimitsRepository, never()).findByOwnerAndCurrencyAndActionAndWalletType(
            walletOwner.id()!!,
            "ETH",
            "withdraw",
            "main"
        )
        verify(walletLimitsRepository, never()).findByLevelAndCurrencyAndActionAndWalletType(
            "1",
            "ETH",
            "withdraw",
            "main"
        )
        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWrongAmount_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(-1)) } }
    }

    @Test
    fun givenWalletWithUnreachedLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "deposit")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "deposit",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "deposit", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "deposit",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "deposit", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    "1",
                    2,
                    "deposit",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(5))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(1))

        assertThat(isAllowed).isTrue()
    }

    @Test
    fun givenWalletWithWalletLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(2, "ETH", 30, "deposit")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "deposit",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(2, "ETH", "deposit", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    null,
                    2,
                    "deposit",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "deposit", "main")
            } doReturn Mono.just(
                WalletLimitsModel(
                    1,
                    "1",
                    2,
                    "deposit",
                    "ETH",
                    "main",
                    30,
                    BigDecimal.valueOf(100),
                    10,
                    BigDecimal.valueOf(3000),
                    300
                )
            )
        }
        val wallet = object : Wallet {
            override fun id() = 30L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(500))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(30))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenIsDepositAllowed_thenReturnFalse(): Unit = runBlocking {
        stubbing(walletLimitsRepository) {
            on {
                findByOwnerAndCurrencyAndWalletAndAction(walletOwner.id()!!, "ETH", 20, "deposit")
            } doReturn Mono.empty()
            on {
                findByOwnerAndCurrencyAndActionAndWalletType(walletOwner.id()!!, "ETH", "deposit", "main")
            } doReturn Mono.empty()
            on {
                findByLevelAndCurrencyAndActionAndWalletType("1", "ETH", "deposit", "main")
            } doReturn Mono.empty()
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        val isAllowed = walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(0.5))

        assertThat(isAllowed).isFalse()
    }

    @Test
    fun givenWalletOwner_whenFindWalletByOwnerAndCurrencyAndType_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletOwnerRepository) {
            on { findById(walletOwner.id()!!) } doReturn Mono.just(
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
        stubbing(walletRepository) {
            on {
                findByOwnerAndTypeAndCurrency(walletOwner.id()!!, "main", currency.getSymbol())
            } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id()!!,
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(1.2)
                )
            )
        }
        val wallet = walletManagerImpl.findWalletByOwnerAndCurrencyAndType(walletOwner, "main", currency)

        assertThat(wallet).isNotNull
        assertThat(wallet!!.owner().id()).isEqualTo(walletOwner.id())
        assertThat(wallet.currency().getSymbol()).isEqualTo(currency.getSymbol())
        assertThat(wallet.type()).isEqualTo("main")
    }

    @Test
    fun givenEmptyWalletWithNoLimit_whenCreateWallet_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                save(WalletModel(null, walletOwner.id()!!, "main", currency.getSymbol(), BigDecimal.valueOf(1)))
            } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id()!!,
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(1)
                )
            )
        }
        val wallet = walletManagerImpl.createWallet(
            walletOwner,
            Amount(currency, BigDecimal.valueOf(1)),
            currency,
            "main"
        )

        assertThat(wallet).isNotNull
        assertThat(wallet.owner().id()).isEqualTo(walletOwner.id())
        assertThat(wallet.currency().getSymbol()).isEqualTo(currency.getSymbol())
        assertThat(wallet.type()).isEqualTo("main")
    }

    @Test
    fun givenWallet_whenIncreaseBalance_thenSuccess(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(20), any())
            } doReturn Mono.just(1)
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        walletManagerImpl.increaseBalance(wallet, BigDecimal.valueOf(1))
    }

    @Test
    fun givenNotExistWallet_whenIncreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(any(), any())
            } doReturn Mono.just(0)
        }
        val wallet = object : Wallet {
            override fun id() = 40L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.increaseBalance(wallet, BigDecimal.valueOf(1)) } }
    }

    @Test
    fun givenWrongAmount_whenIncreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(20), any())
            } doReturn Mono.just(0)
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.increaseBalance(wallet, BigDecimal.valueOf(-1)) } }
    }

    @Test
    fun givenWallet_whenDecreaseBalance_thenSuccess(): Unit = runBlocking {
        stubbing(walletRepository) {
            on { updateBalance(eq(20), eq(BigDecimal.valueOf(-1))) } doReturn Mono.just(1)
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        walletManagerImpl.decreaseBalance(wallet, BigDecimal.valueOf(1))
    }

    @Test
    fun givenNotExist_whenDecreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(any(), any())
            } doReturn Mono.just(0)
        }
        val wallet = object : Wallet {
            override fun id() = 40L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.decreaseBalance(wallet, BigDecimal.valueOf(1)) } }
    }

    @Test
    fun givenWrongAmount_whenDecreaseBalance_thenThrow(): Unit = runBlocking {
        stubbing(walletRepository) {
            on {
                updateBalance(eq(20), any())
            } doReturn Mono.just(0)
        }
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.decreaseBalance(wallet, BigDecimal.valueOf(-1)) } }
    }

    @Test
    fun givenId_whenFindWalletById_thenReturnWallet(): Unit = runBlocking {
        stubbing(walletRepository) {
            on { findById(20) } doReturn Mono.just(
                WalletModel(
                    20L,
                    walletOwner.id()!!,
                    "main",
                    currency.getSymbol(),
                    BigDecimal.valueOf(0.5)
                )
            )
        }
        stubbing(walletOwnerRepository) {
            on {
                findById(walletOwner.id()!!)
            } doReturn Mono.just(
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
        val wallet = walletManagerImpl.findWalletById(20)

        assertThat(wallet).isNotNull
        assertThat(wallet!!.id()).isEqualTo(20)
        assertThat(wallet.balance()).isEqualTo(Amount(currency, BigDecimal.valueOf(0.5)))
        assertThat(wallet.currency().getSymbol()).isEqualTo("ETH")
    }
}
