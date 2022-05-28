package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.accountant.ports.kafka.listener.inout.OrderSubmitRequest

interface OrderSubmitRequestListener : Listener<OrderSubmitRequest>