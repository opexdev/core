package co.nilin.opex.auth.gateway.config

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.util.*


@Configuration
class SystemPropertyConfig {
    @Autowired
    fun addYmlToSystemProperty(
        env: StandardEnvironment,
        beanFactory: ConfigurableBeanFactory
    ) {
        for (propertySource in env.propertySources) {
            if (propertySource is MapPropertySource) {
                val propertySourceName = propertySource.getName()
                val sysProperties: Properties = System.getProperties()
                println("setting sysprops from $propertySourceName")
                val mapPropertySource = propertySource
                println("source.name: ${mapPropertySource.name}")
                for (key in mapPropertySource.propertyNames) {
                    val value = mapPropertySource.getProperty(key)
                    if (sysProperties[key] == null) {
                        if (value is String) {
                            var resolvedValue = beanFactory.resolveEmbeddedValue("\${$key}") ?: value
                            if (resolvedValue.startsWith("classpath:")) {
                                resolvedValue =
                                    ClassPathResource(resolvedValue.substring("classpath:".length)).file.absolutePath
                            }
                            sysProperties[key] = resolvedValue
                        } else {
                            sysProperties[key] = value
                        }
                        println("$key -> ${sysProperties[key]}")
                    }
                }
            }
        }
    }
}
