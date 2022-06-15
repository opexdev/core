package co.nilin.opex.api.ports.postgres.impl.sample

import co.nilin.opex.api.ports.postgres.model.SymbolMapModel
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset

object VALID {
    private const val USER_LEVEL_REGISTERED = "registered"
    private const val TIMESTAMP = 1653125840L
    private val CREATE_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP, 0, ZoneOffset.UTC)
    private val UPDATE_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP + 180, 0, ZoneOffset.UTC)
    private val FROM_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP - 600, 0, ZoneOffset.UTC)
    private val TO_DATE: LocalDateTime = LocalDateTime.ofEpochSecond(TIMESTAMP + 600, 0, ZoneOffset.UTC)

    const val ETH_USDT = "ETH_USDT"

    val PRINCIPAL = Principal { "98c7ca9b-2d9c-46dd-afa8-b0cd2f52a97c" }

    val SYMBOL_MAP_MODEL = SymbolMapModel(
        1,
        ETH_USDT,
        "binance",
        ETH_USDT.replace("_", "")
    )
}
