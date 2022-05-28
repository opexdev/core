package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

interface EventListener : Listener<CoreEvent>