package co.nilin.opex.api.ports.postgres.impl.sample

import co.nilin.opex.api.ports.postgres.model.SymbolMapModel

object VALID {

    const val ETH_USDT = "ETH_USDT"

    val SYMBOL_MAP_MODEL = SymbolMapModel(
        1,
        ETH_USDT,
        "binance",
        ETH_USDT.replace("_", "")
    )
}
