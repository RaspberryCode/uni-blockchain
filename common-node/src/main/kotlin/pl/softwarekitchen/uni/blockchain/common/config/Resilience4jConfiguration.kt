package pl.softwarekitchen.uni.blockchain.common.config
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.RetryConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Resilience4jConfiguration {
    private final val timeBetweenRetries: Long = 1000
    private final val maxNumberOfRetries: Int = 3

    @Bean
    fun retryConfig(): RetryConfig {
        return RetryConfig.custom<Any>()
            .maxAttempts(maxNumberOfRetries)
            .intervalFunction(IntervalFunction.ofExponentialBackoff(timeBetweenRetries, 2.0))
            .retryExceptions(Exception::class.java)
            .build()
    }
}