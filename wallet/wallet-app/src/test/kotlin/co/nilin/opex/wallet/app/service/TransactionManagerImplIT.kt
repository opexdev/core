package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.spi.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@Import(TestChannelBinderConfiguration::class)

class TransactionManagerImplIT {
    @Autowired
    lateinit var transferManager: TransferManager

    @Autowired
    lateinit var currencyService: CurrencyService

    @Autowired
    lateinit var walletManager: WalletManager

    @Autowired
    lateinit var walletOwnerManager: WalletOwnerManager

    @Autowired
    lateinit var transactionManager: TransactionManager

    val senderWalletType = "main"
    val receiverWalletType = "exchange"
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
            val currency = currencyService.getCurrency(cc)!!

            destUuid = UUID.randomUUID().toString()
            setupWallets(destUuid!!)

            val sender = walletOwnerManager.findWalletOwner(sourceUuid!!)!!
            val receiver = walletOwnerManager.findWalletOwner(destUuid!!)!!

            val count = 5
            for (i in 1..count) {
                val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sender, senderWalletType, currency)
                val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(receiver, receiverWalletType, currency)
                transferManager.transfer(
                    TransferCommand(
                        sourceWallet!!,
                        receiverWallet!!,
                        Amount(sourceWallet.currency, amount.divide(BigDecimal.valueOf(count * 1L))),
                        "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}",
                        "NORMAL",
                        mapOf(Pair("key", "val"))
                    )
                )
            }

            val thSender = transactionManager.findTransactions(
                sender.uuid, currency.symbol, "NORMAL", LocalDateTime.now().minusHours(1), LocalDateTime.now(), true, 3, 3
            )

            assertEquals(2, thSender.size)
            assertTrue(thSender.first().date.compareTo(thSender.last().date) < 0)


            val thReceiver = transactionManager.findTransactions(
                receiver.uuid, currency.symbol, "NORMAL", LocalDateTime.now().minusHours(1), LocalDateTime.now(), false, 3, 1
            )

            assertEquals(3, thReceiver.size)
            assertTrue(thReceiver.first().date.compareTo(thReceiver.last().date) > 0)

            val thReceiverAll = transactionManager.findTransactions(
                receiver.uuid, null, null, LocalDateTime.now().minusHours(1), LocalDateTime.now(), true, 100, 0
            )
            assertEquals(count, thReceiverAll.size)

        }
    }

    fun setupWallets(sourceUuid: String) {
        runBlocking {
            var currency = currencyService.getCurrency(cc)
            if (currency == null) {
                currencyService.deleteCurrency(cc)
                currencyService.addCurrency(cc, cc, BigDecimal.ONE)
                currency = currencyService.getCurrency(cc)
            }
            val sourceOwner = walletOwnerManager.createWalletOwner(sourceUuid, "not set", "")
            walletManager.createWallet(sourceOwner, Amount(currency!!, amount.multiply(BigDecimal.valueOf(2))), currency, senderWalletType)
            walletManager.createWallet(
                sourceOwner,
                Amount(currency, BigDecimal.ZERO),
                currency,
                receiverWalletType
            )

        }
    }


}

