package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.FiatScannerResponse
import co.nilin.opex.api.core.inout.PodiumStatementsRequest

interface FiatScannerProxy {

    suspend fun fetchPodiumStatements(request: PodiumStatementsRequest): FiatScannerResponse

    /** [status], [limit] and [offset] are all optional; upstream applies its own defaults. */
    suspend fun listWebhooks(status: String?, limit: Int?, offset: Int?): FiatScannerResponse

    suspend fun retryWebhook(id: Long): FiatScannerResponse
}
