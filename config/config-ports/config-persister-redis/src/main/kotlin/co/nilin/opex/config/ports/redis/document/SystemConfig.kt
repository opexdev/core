package co.nilin.opex.config.ports.redis.document

import co.nilin.opex.config.core.inout.ConsumerModule
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.index.Indexed

data class SystemConfig(
    @Id
    @Indexed
    val key: String,
    val value: String,
    val consumerName: ConsumerModule
)