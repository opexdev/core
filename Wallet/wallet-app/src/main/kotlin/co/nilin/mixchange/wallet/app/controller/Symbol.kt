package co.nilin.mixchange.wallet.app.controller

import co.nilin.mixchange.wallet.core.model.Currency

class Symbol(val symbol_: String): Currency {
    override fun getSymbol(): String {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        return symbol_
    }

    override fun getPrecision(): Int {
        TODO("Not yet implemented")
    }
}