package co.nilin.opex.api.app.utils

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.health.HealthComponent
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.actuate.health.SystemHealth
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

//@Component
class PrometheusHealthExtension(
    private val registry: MeterRegistry,
    private val endpoint: HealthEndpoint
) {

    private var consulHealth = -1
    private var r2dbcHealth = -1
    private var vaultHealth = -1
    private var vaultReactiveHealth = -1
    private val service = "API"

    init {
        Gauge.builder("consul_health", consulHealth) { consulHealth.toDouble() }
            .description("Health of consul connection")
            .tag("Service", service)
            .register(registry)

        Gauge.builder("r2dbc_health", r2dbcHealth) { r2dbcHealth.toDouble() }
            .description("Health of r2dbc connection")
            .tag("Service", service)
            .register(registry)

        Gauge.builder("vault_health", vaultHealth) { vaultHealth.toDouble() }
            .description("Health of vault connection")
            .tag("Service", service)
            .register(registry)

        Gauge.builder("vaultReactive_health", vaultReactiveHealth) { vaultReactiveHealth.toDouble() }
            .description("Health of vaultReactive connection")
            .tag("Service", service)
            .register(registry)
    }

    //@Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun updateHealth() {
        try {
            val health = endpoint.health() as SystemHealth
            consulHealth = getHealthValue(health.components["consul"])
            r2dbcHealth = getHealthValue(health.components["r2dbc"])
            vaultHealth = getHealthValue(health.components["vault"])
            vaultReactiveHealth = getHealthValue(health.components["vaultReactive"])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getHealthValue(health: HealthComponent?): Int {
        health ?: return -1
        return if (health.status.code == "UP") 1 else 0
    }

}