package co.nilin.opex.api.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import springfox.documentation.swagger2.annotations.EnableSwagger2


@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableSwagger2
class ApiApp

fun main(args: Array<String>) {
    runApplication<ApiApp>(*args)
}
