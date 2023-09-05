package co.nilin.opex.admin.core.spi

interface WhiteListPersister {

    fun addToWhiteList(users:List<String>)

    fun deleteFromWhiteList(users: List<String>)


    fun getWhiteList():List<String>?
}