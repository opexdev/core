package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand

interface CryptoCurrencyHandlerV2 {


    suspend fun createImpl(request:CryptoCurrencyCommand)

    suspend fun updateImpl(request:CryptoCurrencyCommand):CryptoCurrencyCommand?

    suspend fun fetchCurrencyImpls(currencyUuid:String)


    suspend fun fetchImpls(request:String)

    suspend fun fetchImpl(request:String)




}
