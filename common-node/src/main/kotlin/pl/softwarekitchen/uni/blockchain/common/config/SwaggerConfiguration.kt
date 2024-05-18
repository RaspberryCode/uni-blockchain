package pl.softwarekitchen.uni.blockchain.common.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SwaggerConfiguration : WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        val swaggerHomePage = "swagger-ui/index.html"
        registry.addRedirectViewController("/", swaggerHomePage)
        registry.addRedirectViewController("/docs", swaggerHomePage)
        registry.addRedirectViewController("/swagger-ui.html", swaggerHomePage)
    }

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Blockchain API")
                    .version("1.0")
                    .description("API documentation for the Blockchain Node")
                    .contact(
                        Contact().name("Software Kitchen")
                            .email("tmalinowski@softwarekitchen.pl")
                    )
            )
    }

//    @Bean
//    fun publicApi(): GroupedOpenApi {
//        return GroupedOpenApi.builder()
//            .group("public")
//            .packagesToScan("pl.softwarekitchen")
//            .build()
//    }
}