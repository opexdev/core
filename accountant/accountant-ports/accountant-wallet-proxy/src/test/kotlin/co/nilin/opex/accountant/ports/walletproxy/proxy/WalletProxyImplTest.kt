package co.nilin.opex.accountant.ports.walletproxy.proxy

import co.nilin.opex.accountant.core.model.WalletType
import co.nilin.opex.accountant.ports.walletproxy.data.Amount
import co.nilin.opex.accountant.ports.walletproxy.data.Currency
import co.nilin.opex.accountant.ports.walletproxy.data.TransferResult
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

class WalletProxyImplTest {

    private lateinit var mockServer: MockServerClient
    private val walletProxyImpl = WalletProxyImpl(
        WebClient.builder().build(),
        "http://localhost:8089"
    )
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        mockServer = ClientAndServer.startClientAndServer(8089)
    }

    @AfterEach
    fun tearDown() {
        mockServer.close()
    }

    @Test
    fun givenAdditionalData_whenTransfer_ok() {
        val symbol = "ETHBTC"
        val senderWalletType = WalletType.MAIN
        val senderUuid = "1"
        val receiverWalletType = WalletType.EXCHANGE
        val receiverUuid = "2"
        val amount = BigDecimal.ONE
        val description = "desc"
        val transferRef = "ref"
        val transferCategory = "ORDER_CREATE"
        val amountObject = Amount(Currency(symbol, symbol, 1), amount)

        mockServer.`when`(
            request().withMethod("POST")
                .withPath("/v2/transfer/${amount}_$symbol/from/${senderUuid}_$senderWalletType/to/${receiverUuid}_$receiverWalletType")
                .withBody(
                    objectMapper.writeValueAsString(
                        WalletProxyImpl.TransferBody(
                            description,
                            transferRef,
                            transferCategory
                        )
                    )
                )
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(
                    objectMapper.writeValueAsString(
                        TransferResult(
                            System.currentTimeMillis(),
                            senderUuid,
                            senderWalletType,
                            amountObject,
                            amountObject,
                            amountObject,
                            receiverUuid,
                            receiverWalletType,
                            amountObject
                        )
                    )
                )
        )

        runBlocking {
            walletProxyImpl.transfer(
                symbol,
                senderWalletType,
                senderUuid,
                receiverWalletType,
                receiverUuid,
                amount,
                description,
                transferRef,
                transferCategory
            )
        }
    }

}