package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.inout.FinancialActionResponseEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.FAResponseListener
import org.springframework.stereotype.Component

@Component
class FAResponseKafkaListener : EventConsumer<FAResponseListener, String, FinancialActionResponseEvent>()