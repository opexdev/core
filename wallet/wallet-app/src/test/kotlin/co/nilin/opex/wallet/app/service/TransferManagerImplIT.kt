package co.nilin.opex.wallet.app.service

import co.nilin.opex.wallet.app.KafkaEnabledTest
import co.nilin.opex.wallet.core.exc.ConcurrentBalanceChangException
import co.nilin.opex.wallet.core.inout.CurrencyCommand
import co.nilin.opex.wallet.core.inout.TransferCommand
import co.nilin.opex.wallet.core.model.Amount
import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.FetchCurrency
import co.nilin.opex.wallet.core.spi.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


class TransferManagerImplIT : KafkaEnabledTest() {
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
    fun givenSameSenderWallet_whenConcurrentTransfers_thenSecondTransferFail() {

        val block: () -> Unit = {
            runBlocking {
                val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!
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
                            "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}", TransferCategory.NORMAL
                        )
                    )
                }
                launch {
                    transferManager.transfer(
                        TransferCommand(
                            sourceWallet!!,
                            receiverWallet!!,
                            Amount(sourceWallet.currency, amount),
                            "Amount2 ${System.currentTimeMillis()}", "Ref2 ${System.currentTimeMillis()}", TransferCategory.NORMAL
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
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!
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
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!
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
                        "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}", TransferCategory.NORMAL
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
                        "Amount2 ${System.currentTimeMillis()}", "Ref2 ${System.currentTimeMillis()}", TransferCategory.NORMAL
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
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!
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
                        "Amount1 ${System.currentTimeMillis()}", "Ref1 ${System.currentTimeMillis()}", TransferCategory.NORMAL
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
                        "Amount2 ${System.currentTimeMillis()}", "Ref2 ${System.currentTimeMillis()}", TransferCategory.NORMAL,
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
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!
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
                    TransferCategory.NORMAL
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
                TransferCategory.NORMAL,
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
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))!!

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
                    TransferCategory.NORMAL
                )
            )

            val thw = transactionManager.findWithdrawTransactions(
                sender.uuid, currency.symbol, LocalDateTime.now().minusHours(1), LocalDateTime.now(), 100, 0
            )

            val thd = transactionManager.findDepositTransactions(
                receiver.uuid, currency.symbol, LocalDateTime.now().minusHours(1), LocalDateTime.now(), 100, 0
            )

            val thwMatch = thw.find { th -> th.id.toString() == result.tx }
            assertNotNull(thwMatch)
            assertEquals(TransferCategory.NORMAL, thwMatch!!.category)

            val thdMatch = thd.find { th -> th.id.toString() == result.tx }
            assertNotNull(thdMatch)
            assertEquals(TransferCategory.NORMAL, thdMatch!!.category)

            val thSender = transactionManager.findTransactions(
                sender.uuid,
                currency.symbol,
                TransferCategory.NORMAL,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                true,
                100,
                0
            )
            val thSenderMatch = thSender.find { i -> i.id.toString().equals(result.tx) }
            assertEquals(sender.uuid, thSenderMatch!!.senderUuid)
            assertEquals(TransferCategory.NORMAL, thSenderMatch.category)


            val thReceiver = transactionManager.findTransactions(
                receiver.uuid,
                currency.symbol,
                TransferCategory.NORMAL,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                true,
                100,
                0
            )
            val thReceiverMatch = thReceiver.find { i -> i.id.toString().equals(result.tx) }
            assertEquals(receiver.uuid, thReceiverMatch!!.receiverUuid)
            assertEquals(TransferCategory.NORMAL, thReceiverMatch.category)

        }
    }

    fun setupWallets(sourceUuid: String) {
        runBlocking {
            try {
                currencyService.deleteCurrency(FetchCurrency(symbol = cc))
            } catch (_: Exception) {

            }
            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))?.let { it } ?: run {
                currencyService.createNewCurrency(CurrencyCommand(name = cc, symbol = cc, precision = BigDecimal.ONE))
            }


//            val currency = currencyService.fetchCurrency(FetchCurrency(symbol = cc))

            val sourceOwner = walletOwnerManager.createWalletOwner(sourceUuid, "not set", "")
            walletManager.createWallet(
                sourceOwner,
                Amount(currency!!, amount.multiply(BigDecimal.valueOf(2))),
                currency,
                WalletType.MAIN
            )


        }
    }


}

