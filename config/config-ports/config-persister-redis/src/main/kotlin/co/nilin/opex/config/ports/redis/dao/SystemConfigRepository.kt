package co.nilin.opex.config.ports.redis.dao

import co.nilin.opex.config.ports.redis.document.SystemConfigDocument
import com.redis.om.spring.repository.RedisDocumentRepository
import org.springframework.stereotype.Component

@Component
interface SystemConfigRepository : RedisDocumentRepository<SystemConfigDocument, String> {
}