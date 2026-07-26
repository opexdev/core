package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.DepositListener
import org.springframework.stereotype.Component

@Component
class DepositKafkaListener : EventConsumer<DepositListener, String, DepositEvent>()