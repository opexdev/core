package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class TransactionManagerImplIT : KafkaEnabledTest() {
    @Autowired
    lateinit var transferManager: TransferManager

    @Autowired
    lateinit var currencyService: CurrencyServiceManager

    @Autowired
    lateinit var walletManager: WalletManager

    @Autowired
    lateinit var walletOwnerManager: WalletOwnerManager

    @Autowired
    lateinit var transactionManager: TransactionManager

    val cc = "CC"
    val amount = BigDecimal.valueOf(10)
    var sourceUuid: String? = null
    var destUuid: String? = null

    @BeforeEach
    fun setup() {
        sourceUuid = UUID.randomUUID().toString()
        setupWallets(sourceUuid!!)
    }

    @Test
    fun givenMultipleTransfer_whenFindTransactions_thenOrderedAndPaginated() {
        runBlocking {
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!

            destUuid = UUID.randomUUID().toString()
            setupWallets(destUuid!!)

            val sender = walletOwnerManager.findWalletOwner(sourceUuid!!)!!
            val receiver = walletOwnerManager.findWalletOwner(destUuid!!)!!

            val count = 5
            for (i in 1..count) {
                val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sender, WalletType.MAIN, currency)
                val receiverWallet =
                    walletManager.findWalletByOwnerAndCurrencyAndType(receiver, WalletType.EXCHANGE, currency)

                transferManager.transfer(
                    TransferCommand(
                        sourceWallet!!,
                        receiverWallet!!,
                        Amount(sourceWallet.currency, amount.divide(BigDecimal.valueOf(count * 1L))),
                        "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}",
                        TransferCategory.NORMAL
                    )
                )
            }

            val thSender = transactionManager.findTransactions(
                sender.uuid,
                currency.symbol,
                TransferCategory.NORMAL,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                true,
                3,
                3
            )

            assertEquals(2, thSender.size)
            assertTrue(thSender.first().date.compareTo(thSender.last().date) < 0)
            assertTrue(thSender.all { th -> th.senderUuid == sender.uuid })
            assertTrue(thSender.all { th -> th.srcWalletType == WalletType.MAIN })

            val thReceiver = transactionManager.findTransactions(
                receiver.uuid,
                currency.symbol,
                TransferCategory.NORMAL,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                false,
                3,
                1
            )

            assertEquals(3, thReceiver.size)
            assertTrue(thReceiver.first().date.compareTo(thReceiver.last().date) > 0)
            assertTrue(thReceiver.all { th -> th.receiverUuid == receiver.uuid })
            assertTrue(thReceiver.all { th -> th.destWalletType == WalletType.EXCHANGE })

            val thReceiverAll = transactionManager.findTransactions(
                receiver.uuid, null, null, LocalDateTime.now().minusHours(1), LocalDateTime.now(), true, 100, 0
            )
            assertEquals(count, thReceiverAll.size)
        }
    }

    fun setupWallets(sourceUuid: String) {
        runBlocking {
            try {
                currencyService.deleteCurrency(FetchCurrency(symbol = cc))
            } catch (_: Exception) {

            }
            currencyService.createNewCurrency(CurrencyCommand(name = cc, symbol = cc, precision = BigDecimal.ONE), true)
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))
            val sourceOwner = walletOwnerManager.createWalletOwner(sourceUuid, "not set", "")
            walletManager.createWallet(
                sourceOwner,
                Amount(currency!!, amount.multiply(BigDecimal.valueOf(2))),
                currency,
                WalletType.MAIN
            )
            walletManager.createWallet(
                sourceOwner,
                Amount(currency, BigDecimal.ZERO),
                currency,
                WalletType.EXCHANGE
            )

        }
    }


}

