package co.nilin.opex.config.ports.redis.dto

import co.nilin.opex.config.ports.redis.dao.SystemConfigRepository
import co.nilin.opex.config.ports.redis.document.SystemConfig
import org.springframework.stereotype.Component

@Component
class TestController(private val systemConfigRepository: SystemConfigRepository) {

    init {
        systemConfigRepository.save(
            SystemConfig("g","f","2","f", listOf("sdfs","2342"),"f","d","s","s")
        )
    }

}