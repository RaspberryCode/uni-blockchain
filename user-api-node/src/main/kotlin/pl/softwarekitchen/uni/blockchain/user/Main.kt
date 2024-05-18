package pl.softwarekitchen.uni.blockchain.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["pl.softwarekitchen.uni.blockchain"],
    exclude = [DataSourceAutoConfiguration::class]
)
class UserApiApplication

fun main(args: Array<String>) {
    runApplication<UserApiApplication>(*args)
}


