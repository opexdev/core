package co.nilin.opex.admin.app.service

import co.nilin.opex.admin.core.spi.WhiteListPersister

class WhiteListManagement(private val whiteListPersister: WhiteListPersister) {
    override fun addToWhiteList(users: List<String>) {
        TODO("Not yet implemented")
    }

    override fun deleteFromWhiteList(users: List<String>) {
        TODO("Not yet implemented")
    }

    override fun getWhiteList(): List<String>? {
        TODO("Not yet implemented")
    }
}