package co.nilin.opex.accountant.ports.kafka.listener.spi

import co.nilin.opex.accountant.ports.kafka.listener.inout.FinancialActionResponseEvent

interface FAResponseListener : Listener<FinancialActionResponseEvent>