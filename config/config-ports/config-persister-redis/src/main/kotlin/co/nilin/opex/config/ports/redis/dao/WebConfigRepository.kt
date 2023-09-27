package co.nilin.opex.config.ports.redis.dao

import co.nilin.opex.config.ports.redis.document.WebConfigDocument
import com.redis.om.spring.repository.RedisDocumentRepository
import org.springframework.stereotype.Component

@Component
interface WebConfigRepository : RedisDocumentRepository<WebConfigDocument, String> {
}