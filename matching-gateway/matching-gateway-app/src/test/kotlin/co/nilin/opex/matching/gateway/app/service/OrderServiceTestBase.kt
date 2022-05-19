package co.nilin.opex.matching.gateway.app.service

import co.nilin.opex.matching.gateway.app.spi.AccountantApiProxy
import co.nilin.opex.matching.gateway.app.spi.PairConfigLoader
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.EventSubmitter
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.KafkaHealthIndicator
import co.nilin.opex.matching.gateway.ports.kafka.submitter.service.OrderSubmitter
import org.mockito.kotlin.mock

internal open class OrderServiceTestBase {
    protected val accountantApiProxy: AccountantApiProxy = mock()
    protected val orderSubmitter: OrderSubmitter = mock()
    protected val eventSubmitter: EventSubmitter = mock()
    protected val pairConfigLoader: PairConfigLoader = mock()
    protected val kafkaHealthIndicator: KafkaHealthIndicator = mock()
    protected val orderService: OrderService = OrderService(
        accountantApiProxy,
        orderSubmitter,
        eventSubmitter,
        pairConfigLoader,
        kafkaHealthIndicator
    )
}
