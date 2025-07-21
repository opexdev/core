package co.nilin.opex.profile.core.spi

interface AuthProxy {

    suspend fun updateEmail(userId: String, email: String)
    suspend fun updateMobile(userId: String, mobile: String)
    suspend fun updateName(userId: String, firstName: String , lastName: String)

}