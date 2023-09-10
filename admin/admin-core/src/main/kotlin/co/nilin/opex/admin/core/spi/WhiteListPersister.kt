package co.nilin.opex.admin.core.spi

import co.nilin.opex.admin.core.data.WhitelistAdaptor

interface WhiteListPersister {

    suspend fun addToWhiteList(users: WhitelistAdaptor):WhitelistAdaptor?

    suspend fun deleteFromWhiteList(users: WhitelistAdaptor):WhitelistAdaptor?


    suspend fun getWhiteList(): WhitelistAdaptor?
}