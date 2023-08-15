package co.nilin.opex.accountant.app.scheduler

import co.nilin.opex.accountant.core.api.FinancialActionJobManager
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.FinancialActionCategory
import co.nilin.opex.accountant.core.model.FinancialActionStatus
import co.nilin.opex.accountant.core.spi.FinancialActionLoader
import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.core.spi.JsonMapper
import co.nilin.opex.accountant.core.spi.WalletProxy
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.InOrder
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


@SpringBootTest
@ActiveProfiles("test")
@Import(TestChannelBinderConfiguration::class)
class FinancialActionJobManagerIT {

    @Autowired
    lateinit var financialActionJobManager: FinancialActionJobManager

    @Autowired
    lateinit var financialActionLoader: FinancialActionLoader

    @Autowired
    lateinit var financialActionPersister: FinancialActionPersister

    @Autowired
    lateinit var jsonMapper: JsonMapper

    @MockBean
    lateinit var walletProxy: WalletProxy

    @Test
    fun givenCreatedParentChildActions_whenProcessFinancialActions_thenProcessAllKeepOrder() {
        val uuid = UUID.randomUUID().toString()
        val ouid = UUID.randomUUID().toString()
        val symbol = "SY"
        val parent1 = FinancialAction(
            null,
            "Parent",
            ouid,
            symbol,
            BigDecimal.TEN,
            uuid,
            "main",
            uuid,
            "exchange",
            LocalDateTime.now(),
            FinancialActionCategory.ORDER_CREATE
        )

        runBlocking {
            financialActionPersister.persist(
                listOf(parent1)
            )
            val parent1Saved = financialActionLoader.findLast(uuid, ouid)!!
            val child1 = FinancialAction(
                parent1Saved,
                "Child",
                ouid,
                symbol,
                BigDecimal.TEN,
                uuid,
                "exchange",
                uuid,
                "main",
                LocalDateTime.now(),
                FinancialActionCategory.TRADE
            )
            val parent2 = FinancialAction(
                null,
                "Parent",
                UUID.randomUUID().toString(),
                symbol,
                BigDecimal.ONE,
                uuid,
                "main",
                uuid,
                "exchange",
                LocalDateTime.now(),
                FinancialActionCategory.FEE
            )

            financialActionPersister.persist(listOf(child1, parent2))

            financialActionJobManager.processFinancialActions(0, 100);

            assertEquals(0, financialActionLoader.countUnprocessed(uuid, symbol, child1.eventType))
            val orderVerifier = Mockito.inOrder(walletProxy)
            verifyTransfer(orderVerifier, parent1)
            verifyTransfer(orderVerifier, child1)
            verifyTransfer(orderVerifier, parent2)
        }

    }

    @Test
    fun givenFailedParentCreatedChildActions_whenProcessFinancialActions_thenSkipParentAndChild() {
        val uuid = UUID.randomUUID().toString()
        val ouid = UUID.randomUUID().toString()
        val symbol = "SY"
        val parent1 = FinancialAction(
            null,
            "Parent",
            ouid,
            symbol,
            BigDecimal.TEN,
            uuid,
            "main",
            uuid,
            "exchange",
            LocalDateTime.now(),
            FinancialActionCategory.ORDER_CREATE
        )

        runBlocking {
            financialActionPersister.persist(
                listOf(parent1)
            )
            val parent1Saved = financialActionLoader.findLast(uuid, ouid)!!
            financialActionPersister.updateStatus(parent1Saved, FinancialActionStatus.ERROR)

            val child1 = FinancialAction(
                parent1Saved,
                "Child",
                ouid,
                symbol,
                BigDecimal.TEN,
                uuid,
                "exchange",
                uuid,
                "main",
                LocalDateTime.now(),
                FinancialActionCategory.TRADE
            )
            val parent2 = FinancialAction(
                null,
                "Parent",
                UUID.randomUUID().toString(),
                symbol,
                BigDecimal.ONE,
                uuid,
                "main",
                uuid,
                "exchange",
                LocalDateTime.now(),
                FinancialActionCategory.ORDER_CREATE
            )

            financialActionPersister.persist(listOf(child1, parent2))
            financialActionJobManager.processFinancialActions(0, 100);

            assertEquals(1, financialActionLoader.countUnprocessed(uuid, symbol, child1.eventType))
            val orderVerifier = Mockito.inOrder(walletProxy)
            verifyTransfer(orderVerifier, parent2)
        }

    }

    @Test
    fun givenCreatedParentChildActions_whenProcessFinancialActionsParentFail_thenSkipChild() {
        val uuid = UUID.randomUUID().toString()
        val ouid = UUID.randomUUID().toString()
        val symbol = "SY"
        val parent1 = FinancialAction(
            null,
            "Parent",
            ouid,
            symbol,
            BigDecimal.TEN,
            uuid,
            "main",
            uuid,
            "exchange",
            LocalDateTime.now(),
            FinancialActionCategory.ORDER_CREATE
        )

        runBlocking {
            financialActionPersister.persist(
                listOf(parent1)
            )
            val parent1Saved = financialActionLoader.findLast(uuid, ouid)!!
            val child1 = FinancialAction(
                parent1Saved,
                "Child",
                ouid,
                symbol,
                BigDecimal.TEN,
                uuid,
                "exchange",
                uuid,
                "main",
                LocalDateTime.now(),
                FinancialActionCategory.TRADE
            )
            val parent2 = FinancialAction(
                null,
                "Parent",
                UUID.randomUUID().toString(),
                symbol,
                BigDecimal.ONE,
                uuid,
                "main",
                uuid,
                "exchange",
                LocalDateTime.now(),
                FinancialActionCategory.ORDER_CREATE
            )

            financialActionPersister.persist(listOf(child1, parent2))

            `when`(
                walletProxy.transfer(
                    parent1.symbol,
                    parent1.senderWalletType,
                    parent1.sender,
                    parent1.receiverWalletType,
                    parent1.receiver,
                    parent1.amount,
                    parent1.eventType + parent1.pointer,
                    parent1Saved.id.toString(),
                    parent1.category.toString(),
                    parent1.detail
                )
            ).thenAnswer {
                throw Exception("transfer failed")
            }
            financialActionJobManager.processFinancialActions(0, 100);

            assertEquals(1, financialActionLoader.countUnprocessed(uuid, symbol, child1.eventType))
            val orderVerifier = Mockito.inOrder(walletProxy)
            verifyTransfer(orderVerifier, parent1)
            verifyTransfer(orderVerifier, parent2)
        }

    }


    private suspend fun verifyTransfer(orderVerifier: InOrder, fi: FinancialAction) {
        orderVerifier.verify(walletProxy).transfer(
            eq(fi.symbol), eq(fi.senderWalletType), eq(fi.sender), eq(fi.receiverWalletType), eq(fi.receiver), eq(fi.amount), eq(fi.eventType + fi.pointer), any(), eq(fi.category.toString()), eq(fi.detail),
        )
    }
}
