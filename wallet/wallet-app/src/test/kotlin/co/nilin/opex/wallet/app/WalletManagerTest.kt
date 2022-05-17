package co.nilin.opex.wallet.app

import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.Currency
import co.nilin.opex.wallet.core.model.Wallet
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private class WalletManagerTest : WalletManagerTestBase() {
    //region isWithdrawAllowed()
    @Test
    fun givenFullWalletWithNoLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
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
    fun givenInsufficientAmount_whenIsWithdrawAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isWithdrawAllowed(wallet, BigDecimal.valueOf(0.5)) } }
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
    fun givenWalletWithUnreachedLimit_whenIsWithdrawAllowed_thenReturnTrue(): Unit = runBlocking {
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
    fun givenWalletWithWalletLimit_whenIsWithdrawAllowed_thenReturnFalse(): Unit = runBlocking {
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
    //endregion

    //region isDepositAllowed()
    @Test
    fun givenFullWalletWithNoLimit_whenIsDepositAllowed_thenReturnTrue(): Unit = runBlocking {
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
    fun givenInsufficientAmount_whenIsDepositAllowed_thenThrow(): Unit = runBlocking {
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(0))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.isDepositAllowed(wallet, BigDecimal.valueOf(0.5)) } }
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
    //endregion

    //region findWalletByOwnerAndCurrencyAndType()
    @Test
    fun givenWalletOwner_whenFindWalletByOwnerAndCurrencyAndType_thenReturnWallet(): Unit = runBlocking {
        val wallet = walletManagerImpl.findWalletByOwnerAndCurrencyAndType(walletOwner, "main", currency)
        assertThat(wallet).isNotNull
        assertThat(wallet!!.owner().id()).isEqualTo(walletOwner.id())
        assertThat(wallet.currency().getSymbol()).isEqualTo(currency.getSymbol())
        assertThat(wallet.type()).isEqualTo("main")
    }
    //endregion

    //region createWallet()
    @Test
    fun givenEmptyWalletWithNoLimit_whenCreateWallet_thenReturnWallet(): Unit = runBlocking {
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
    //endregion

    //region increaseBalance()
    @Test
    fun givenWallet_whenIncreaseBalance_thenSuccess(): Unit = runBlocking {
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
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.increaseBalance(wallet, BigDecimal.valueOf(-1)) } }
    }
    //endregion

    //region decreaseBalance()
    @Test
    fun givenWallet_whenDecreaseBalance_thenSuccess(): Unit = runBlocking {
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
        val wallet = object : Wallet {
            override fun id() = 20L
            override fun owner() = walletOwner
            override fun balance() = Amount(currency, BigDecimal.valueOf(2))
            override fun currency() = currency
            override fun type() = "main"
        }

        assertThatThrownBy { runBlocking { walletManagerImpl.decreaseBalance(wallet, BigDecimal.valueOf(-1)) } }
    }
    //endregion

    //region findWalletById()
    @Test
    fun givenId_whenFindWalletById_thenReturnWallet(): Unit = runBlocking {
        val wallet = walletManagerImpl.findWalletById(20)

        assertThat(wallet).isNotNull
        assertThat(wallet!!.id()).isEqualTo(20)
    }
    //endregion
}
