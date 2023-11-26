package co.nilin.opex.wallet.app.controller

import co.nilin.opex.wallet.app.dto.TransactionRequest
import co.nilin.opex.wallet.core.model.TransactionHistory
import co.nilin.opex.wallet.core.spi.TransactionManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)

class TransactionControllerTest {
    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var manager: TransactionManager

    @Test
    fun whenGetTransactionsForUser_thenReturnsHistory() {
        val uuid = "uuid";
        val t = System.currentTimeMillis()
        val history = TransactionHistory(
            1L, "c", BigDecimal.ONE, "desc", "ref", System.currentTimeMillis(), "cat", mapOf(Pair("key1", "val1")), true
        )
        runBlocking {
            Mockito.`when`(
                manager.findTransactions(
                    uuid, "c", null, LocalDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault()), LocalDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault()), true, 1, 1
                )
            ).thenReturn(listOf(history))
            webClient.post().uri("/transaction/$uuid").accept(MediaType.APPLICATION_JSON)
                .bodyValue(TransactionRequest("c", null, t, t, 1, 1, true))
                .exchange()
                .expectStatus().isOk
                .expectBodyList(TransactionHistory::class.java)
                .contains(history)
        }

    }
}
