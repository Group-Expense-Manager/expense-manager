package pl.edu.agh.gem.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.helper.http.GemRestTemplateFactory
import java.time.Duration

@Configuration
class ClientConfig {
    @Bean
    @Qualifier("GroupManagerRestTemplate")
    fun groupManagerRestTemplate(
        groupManagerProperties: GroupManagerProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(groupManagerProperties.readTimeout)
            .withConnectTimeout(groupManagerProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("CurrencyManagerRestTemplate")
    fun currencyManagerRestTemplate(
        currencyManagerProperties: CurrencyManagerProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(currencyManagerProperties.readTimeout)
            .withConnectTimeout(currencyManagerProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("FinanceAdapterRestTemplate")
    fun financeAdapterRestTemplate(
        financeAdapterProperties: FinanceAdapterProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(financeAdapterProperties.readTimeout)
            .withConnectTimeout(financeAdapterProperties.connectTimeout)
            .build()
    }
}

@ConfigurationProperties(prefix = "group-manager")
data class GroupManagerProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "currency-manager")
data class CurrencyManagerProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "finance-adapter")
data class FinanceAdapterProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)
