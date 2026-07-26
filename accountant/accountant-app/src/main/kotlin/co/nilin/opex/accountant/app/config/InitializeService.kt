package co.nilin.opex.accountant.app.config

import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService{

    @PostConstruct
    fun init() = runBlocking {
        // addUserLevels()
        // addPairConfigs()
        // addPairFeeConfigs
    }
}
