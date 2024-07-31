package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.core.exc.ConcurrentBalanceChangException
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.spi.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


class TransferManagerImplIT : KafkaEnabledTest() {

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
    fun givenSameSenderWallet_whenConcurrentTransfers_thenSecondTransferFail() {

        val block: () -> Unit = {
            runBlocking {
                val currency = currencyService.getCurrency(cc)!!
                val owner = walletOwnerManager.findWalletOwner(sourceUuid!!)
                val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.MAIN, currency)
                val receiverWallet =
                    walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

                launch {
                    transferManager.transfer(
                        TransferCommand(
                            sourceWallet!!,
                            receiverWallet!!,
                            Amount(sourceWallet.currency, amount),
                            "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}", "NORMAL"
                        )
                    )
                }
                launch {
                    transferManager.transfer(
                        TransferCommand(
                            sourceWallet!!,
                            receiverWallet!!,
                            Amount(sourceWallet.currency, amount),
                            "Amount2 ${System.currentTimeMillis()}", "Ref2 ${System.currentTimeMillis()}", "NORMAL"
                        )
                    )
                }
            }
        }
        try {
            block.invoke()
        } catch (_: ConcurrentBalanceChangException) {

        }
        runBlocking {
            val currency = currencyService.getCurrency(cc)!!
            val owner = walletOwnerManager.findWalletOwner(sourceUuid!!)
            val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.MAIN, currency)
            val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

            assertEquals(amount, sourceWallet!!.balance.amount)
            assertEquals(amount, receiverWallet!!.balance.amount)
        }
    }

    @Test
    fun givenSameReceiverWallet_whenConcurrentTransfers_thenTransfersSuccess() {
        runBlocking {
            val currency = currencyService.getCurrency(cc)!!
            val owner = walletOwnerManager.findWalletOwner(sourceUuid!!)
            val receiverWallet =
                walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.EXCHANGE, currency)

            val source2Uuid = UUID.randomUUID().toString()
            setupWallets(source2Uuid)
            val sourceOwner2 = walletOwnerManager.findWalletOwner(source2Uuid)

            val t1 = async {
                val sourceWallet1 = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
                transferManager.transfer(
                    TransferCommand(
                        sourceWallet1!!,
                        receiverWallet!!,
                        Amount(sourceWallet1.currency, amount),
                        "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}", "NORMAL"
                    )
                )
            }
            val t2 = async {
                val sourceWallet2 =
                    walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner2!!, WalletType.MAIN, currency)
                transferManager.transfer(
                    TransferCommand(
                        sourceWallet2!!,
                        receiverWallet!!,
                        Amount(sourceWallet2.currency, amount),
                        "Amount2 ${System.currentTimeMillis()}", "Ref2 ${System.currentTimeMillis()}", "NORMAL"
                    )
                )
            }
            t1.await()
            t2.await()

            val sourceWallet1Refresh =
                walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.MAIN, currency)
            val sourceWallet2Refresh =
                walletManager.findWalletByOwnerAndCurrencyAndType(sourceOwner2!!, WalletType.MAIN, currency)
            val receiverWalletRefresh =
                walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

            assertEquals(amount, sourceWallet1Refresh!!.balance.amount)
            assertEquals(amount, sourceWallet2Refresh!!.balance.amount)
            assertEquals(amount.plus(amount), receiverWalletRefresh!!.balance.amount)
        }


    }

    @Test
    fun givenSameSenderWallet_whenSequentialTransfers_thenTransfersSuccess() {
        runBlocking {
            val currency = currencyService.getCurrency(cc)!!
            val owner = walletOwnerManager.findWalletOwner(sourceUuid!!)

            async {
                val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.MAIN, currency)
                val receiverWallet =
                    walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

                transferManager.transfer(
                    TransferCommand(
                        sourceWallet!!,
                        receiverWallet!!,
                        Amount(sourceWallet.currency, amount),
                        "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}", "NORMAL"
                    )
                )
            }.await()
            async {
                val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.MAIN, currency)
                val receiverWallet =
                    walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

                transferManager.transfer(
                    TransferCommand(
                        sourceWallet!!,
                        receiverWallet!!,
                        Amount(sourceWallet.currency, amount),
                        "Amount2 ${System.currentTimeMillis()}", "Ref2 ${System.currentTimeMillis()}", "NORMAL",
                    )
                )
            }.await()
            val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.MAIN, currency)
            val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

            assertEquals(BigDecimal.ZERO, sourceWallet!!.balance.amount)
            assertEquals(amount.plus(amount), receiverWallet!!.balance.amount)
        }
    }

    @Test
    fun dwhenTransferWithAdditionalData_thenDataIsPersistedAndRetrievable() {
        runBlocking {
            val currency = currencyService.getCurrency(cc)!!
            val owner = walletOwnerManager.findWalletOwner(sourceUuid!!)

            val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner!!, WalletType.MAIN, currency)
            val receiverWallet = walletManager.findWalletByOwnerAndCurrencyAndType(owner, WalletType.EXCHANGE, currency)

            val additionalData = mapOf(Pair("key1", "value"), Pair("key2", "value"))
            val result = transferManager.transfer(
                TransferCommand(
                    sourceWallet!!,
                    receiverWallet!!,
                    Amount(sourceWallet.currency, amount),
                    "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}",
                    "NORMAL"
                )
            )

            val thw = transactionManager.findWithdrawTransactions(
                owner.uuid, currency.symbol, LocalDateTime.now().minusHours(1), LocalDateTime.now(), 100, 0
            )

            val thd = transactionManager.findDepositTransactions(
                owner.uuid, currency.symbol, LocalDateTime.now().minusHours(1), LocalDateTime.now(), 100, 0
            )
            val thwMatch = thw.find { th -> th.id.toString().equals(result.tx) }
            assertNotNull(thwMatch)

            val thdMatch = thd.find { th -> th.id.toString().equals(result.tx) }
            assertNotNull(thdMatch)

            val th = transactionManager.findTransactions(
                owner.uuid,
                currency.symbol,
                "NORMAL",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                true,
                100,
                0
            )

            val thMatch = th.find { i -> i.id.toString().equals(result.tx) }
        }
    }

    @Test
    fun whenTransfer_thenWithdrawFlagIsCorrect() {
        runBlocking {
            val currency = currencyService.getCurrency(cc)!!

            destUuid = UUID.randomUUID().toString()
            setupWallets(destUuid!!)

            val sender = walletOwnerManager.findWalletOwner(sourceUuid!!)
            val receiver = walletOwnerManager.findWalletOwner(destUuid!!)


            val sourceWallet = walletManager.findWalletByOwnerAndCurrencyAndType(sender!!, WalletType.MAIN, currency)
            val receiverWallet =
                walletManager.findWalletByOwnerAndCurrencyAndType(receiver!!, WalletType.EXCHANGE, currency)

            val result = transferManager.transfer(
                TransferCommand(
                    sourceWallet!!,
                    receiverWallet!!,
                    Amount(sourceWallet.currency, amount),
                    "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}",
                    "NORMAL"
                )
            )

            val thw = transactionManager.findWithdrawTransactions(
                sender.uuid, currency.symbol, LocalDateTime.now().minusHours(1), LocalDateTime.now(), 100, 0
            )

            val thd = transactionManager.findDepositTransactions(
                receiver.uuid, currency.symbol, LocalDateTime.now().minusHours(1), LocalDateTime.now(), 100, 0
            )

            val thwMatch = thw.find { th -> th.id.toString().equals(result.tx) }
            assertNotNull(thwMatch)
            assertEquals("NORMAL", thwMatch!!.category)

            val thdMatch = thd.find { th -> th.id.toString().equals(result.tx) }
            assertNotNull(thdMatch)
            assertEquals("NORMAL", thdMatch!!.category)

            val thSender = transactionManager.findTransactions(
                sender.uuid,
                currency.symbol,
                "NORMAL",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                true,
                100,
                0
            )
            val thSenderMatch = thSender.find { i -> i.id.toString().equals(result.tx) }
            assertEquals(sender.uuid, thSenderMatch!!.senderUuid)
            assertEquals("NORMAL", thSenderMatch.category)


            val thReceiver = transactionManager.findTransactions(
                receiver.uuid,
                currency.symbol,
                "NORMAL",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                true,
                100,
                0
            )
            val thReceiverMatch = thReceiver.find { i -> i.id.toString().equals(result.tx) }
            assertEquals(receiver.uuid, thReceiverMatch!!.receiverUuid)
            assertEquals("NORMAL", thReceiverMatch.category)

        }
    }

    fun setupWallets(sourceUuid: String) {
        runBlocking {
            try {
                currencyService.deleteCurrency(cc)
            } catch (_: Exception) {

            }
            currencyService.addCurrency(cc, cc, BigDecimal.ONE)
            val currency = currencyService.getCurrency(cc)

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

