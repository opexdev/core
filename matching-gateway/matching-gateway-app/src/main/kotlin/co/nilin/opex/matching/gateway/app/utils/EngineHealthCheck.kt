package co.nilin.opex.matching.gateway.app.utils

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest
import org.springframework.stereotype.Component

@Component
class EngineHealthCheck(private val consulClient: ConsulClient) {

    final var areEnginesUp = true
        private set

    private fun checkServiceHealth(serviceName: String) {
        try {
            val checks = consulClient.getHealthChecksForService(
                serviceName,
                HealthChecksForServiceRequest.newBuilder().build()
            ).value
            areEnginesUp = checks.all { it }
        }
    }
}