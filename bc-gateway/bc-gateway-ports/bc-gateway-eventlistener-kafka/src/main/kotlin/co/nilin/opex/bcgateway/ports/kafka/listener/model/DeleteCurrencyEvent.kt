package co.nilin.opex.bcgateway.ports.kafka.listener.model

data class DeleteCurrencyEvent(val name: String) : AdminEvent()