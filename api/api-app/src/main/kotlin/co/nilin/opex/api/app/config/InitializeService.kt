package co.nilin.opex.api.app.config

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("postgresConfig")
class InitializeService {

    @PostConstruct
    fun init() = runBlocking {
        // Add symbol maps
    }
}
