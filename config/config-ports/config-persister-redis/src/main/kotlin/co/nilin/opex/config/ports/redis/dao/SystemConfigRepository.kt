package co.nilin.opex.config.ports.redis.dao

import co.nilin.opex.config.ports.redis.document.SystemConfig
import com.redis.om.spring.repository.RedisDocumentRepository
import org.springframework.stereotype.Component

@Component
interface SystemConfigRepository : RedisDocumentRepository<SystemConfig, String> {
}