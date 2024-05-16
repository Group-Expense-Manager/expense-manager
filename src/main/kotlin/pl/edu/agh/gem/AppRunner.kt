package pl.edu.agh.gem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AppRunner

fun main(args: Array<String>) {
    runApplication<AppRunner>(*args)
}
