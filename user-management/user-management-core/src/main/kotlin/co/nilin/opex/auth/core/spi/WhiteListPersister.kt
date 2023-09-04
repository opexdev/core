package co.nilin.opex.auth.core.spi

import kotlinx.coroutines.flow.Flow

interface WhiteListPersister {
   fun add(identifier:String)

    fun delete(identifier:String)

    fun getAll(): Flow<String>?

}