package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.UpdateWebConfigRequest
import co.nilin.opex.common.data.WebConfig

interface ConfigProxy {

    suspend fun getWebConfig(): WebConfig
    suspend fun updateWebConfig(token: String, request: UpdateWebConfigRequest) : WebConfig

}
