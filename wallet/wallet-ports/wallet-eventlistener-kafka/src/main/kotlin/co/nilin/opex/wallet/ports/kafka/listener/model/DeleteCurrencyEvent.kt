package co.nilin.opex.wallet.ports.kafka.listener.model

data class DeleteCurrencyEvent(val name: String) : AdminEvent()