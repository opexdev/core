package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CryptoCurrencyCommand
import co.nilin.opex.bcgateway.core.model.CurrencyImps
import co.nilin.opex.bcgateway.core.model.FetchImpls

interface CryptoCurrencyHandlerV2 {


    suspend fun createImpl(request:CryptoCurrencyCommand):CryptoCurrencyCommand?

    suspend fun updateImpl(request:CryptoCurrencyCommand):CryptoCurrencyCommand?

    suspend fun fetchCurrencyImpls(data:FetchImpls):CurrencyImps?





}
