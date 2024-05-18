package pl.softwarekitchen.uni.blockchain.fullnode

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["pl.softwarekitchen.uni.blockchain"])
class FullNodeApplication

fun main(args: Array<String>) {
    runApplication<FullNodeApplication>(*args)
}