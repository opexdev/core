package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.inout.OrderRequestEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.OrderSubmitRequestListener
import org.springframework.stereotype.Component

@Component
class OrderKafkaListener : EventConsumer<OrderSubmitRequestListener, String, OrderRequestEvent>()