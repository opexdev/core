package co.nilin.opex.bcgateway.app.config

import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
@Profile("!otc")
class InitializeService {

    @PostConstruct
    fun init() = runBlocking {
        // addAddressTypes()
        // addChains()
        // addCurrencies()
    }
}
