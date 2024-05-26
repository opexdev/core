package co.nilin.opex.accountant.ports.postgres.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.fi-action")
data class FinancialActionProperties(val retry: RetryConfig)

data class RetryConfig(
    val count: Int,
    val delaySeconds: Long,
    val delayMultiplier: Int
)