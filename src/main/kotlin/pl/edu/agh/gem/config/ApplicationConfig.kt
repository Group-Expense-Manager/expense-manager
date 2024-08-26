package pl.edu.agh.gem.config

import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import com.fasterxml.jackson.module.kotlin.KotlinFeature.SingletonSupport
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.time.ZoneOffset.UTC
import java.util.Locale.ENGLISH
import java.util.TimeZone

@Configuration
class ApplicationConfig {

    @Bean
    fun kotlinModule() = KotlinModule.Builder()
        .withReflectionCacheSize(REFLECTION_CACHE_SIZE)
        .configure(NullToEmptyCollection, false)
        .configure(NullToEmptyMap, false)
        .configure(NullIsSameAsDefault, false)
        .configure(SingletonSupport, false)
        .configure(StrictNullChecks, false)
        .build()

    @Bean
    fun afterburnerModule() = AfterburnerModule()

    @Bean
    fun localeResolver(): LocaleResolver {
        return SessionLocaleResolver().apply {
            this.setDefaultLocale(ENGLISH)
        }
    }

    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone(UTC))
    }

    companion object {
        private const val REFLECTION_CACHE_SIZE = 512
    }
}
