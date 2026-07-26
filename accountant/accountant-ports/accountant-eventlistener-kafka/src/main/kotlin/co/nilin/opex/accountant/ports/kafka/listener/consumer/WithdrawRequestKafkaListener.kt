package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.WithdrawRequestListener
import org.springframework.stereotype.Component

@Component
class WithdrawRequestKafkaListener : EventConsumer<WithdrawRequestListener, String, WithdrawRequestEvent>()