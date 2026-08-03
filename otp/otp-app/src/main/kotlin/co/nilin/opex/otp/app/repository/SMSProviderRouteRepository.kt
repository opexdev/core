package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.SMSProviderRoute
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SMSProviderRouteRepository : CoroutineCrudRepository<SMSProviderRoute, Long> {
    suspend fun findAllByEnabledTrue(): Flux<SMSProviderRoute>
}