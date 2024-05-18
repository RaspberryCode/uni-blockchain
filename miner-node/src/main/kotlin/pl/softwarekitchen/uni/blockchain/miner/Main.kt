package pl.softwarekitchen.uni.blockchain.miner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["pl.softwarekitchen.uni.blockchain"])
class MinerApplication

fun main(args: Array<String>) {
    runApplication<MinerApplication>(*args)
}