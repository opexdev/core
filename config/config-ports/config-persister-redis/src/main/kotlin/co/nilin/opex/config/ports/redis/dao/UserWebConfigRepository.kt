package co.nilin.opex.config.ports.redis.dao

import co.nilin.opex.config.ports.redis.document.UserWebConfigDocument
import com.redis.om.spring.repository.RedisDocumentRepository
import org.springframework.stereotype.Component

@Component
interface UserWebConfigRepository : RedisDocumentRepository<UserWebConfigDocument, String> {



}