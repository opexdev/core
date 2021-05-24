package co.nilin.mixchange.wallet.core.model

interface Currency {
    fun getSymbol(): String
    fun getName(): String
    fun getPrecision(): Int
}