package co.nilin.opex.admin.app.service

import co.nilin.opex.admin.core.data.WhitelistAdaptor
import co.nilin.opex.admin.core.spi.WhiteListPersister
import org.springframework.stereotype.Component

@Component
class WhiteListManagement(private val whiteListPersister: WhiteListPersister) {
    suspend fun addToWhiteList(users: WhitelistAdaptor): WhitelistAdaptor? {
        return whiteListPersister.addToWhiteList(users)
    }

    suspend fun deleteFromWhiteList(users: WhitelistAdaptor): WhitelistAdaptor? {
        return whiteListPersister.deleteFromWhiteList(users)
    }

    suspend fun getWhiteList(): WhitelistAdaptor? {
        return whiteListPersister.getWhiteList()
    }
}