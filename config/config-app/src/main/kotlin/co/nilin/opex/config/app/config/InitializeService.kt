package co.nilin.opex.config.app.config

import co.nilin.opex.utility.preferences.Preferences
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService {

    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {

    }

}
