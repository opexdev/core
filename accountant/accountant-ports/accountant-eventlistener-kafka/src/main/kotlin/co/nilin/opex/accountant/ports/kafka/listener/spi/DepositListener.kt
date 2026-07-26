package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.accountant.ports.kafka.listener.inout.DepositEvent

interface DepositListener : Listener<DepositEvent>