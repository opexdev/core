package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyImps
import co.nilin.opex.bcgateway.core.model.FetchImpls

interface CryptoCurrencyHandlerV2 {


    suspend fun createImpl(request:CryptoCurrencyCommand):CryptoCurrencyCommand?

    suspend fun updateImpl(request:CryptoCurrencyCommand):CryptoCurrencyCommand?

    suspend fun deleteImpl(implUuid:String, currency:String):Void?

    suspend fun fetchCurrencyImpls(data:FetchImpls?=null):CurrencyImps?

    suspend fun fetchImpl(implUuid:String, currency: String):CryptoCurrencyCommand?






}
