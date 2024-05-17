package miner

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.*

@SpringBootApplication
class MinerApplication

fun main(args: Array<String>) {
    runApplication<MinerApplication>(*args)
}

@RestController
@RequestMapping("/blockchain")
class BlockchainController(val blockchainService: BlockchainService) {

    @GetMapping
    fun getBlockchain() =
        blockchainService.blockchain

    @PostMapping("/transactions")
    fun newTransaction(@RequestBody transaction: Transaction): String {
        blockchainService.addTransaction(transaction.sender, transaction.recipient, transaction.amount)
        return "Transaction added"
    }
}

data class Block(
    val index: Int,
    val previousHash: String,
    val timestamp: Long,
    val transactions: List<Transaction>,
    var nonce: Int = 0,
    var hash: String = ""
) {
    fun calculateHash(): String {
        val input = "$previousHash$timestamp$transactions$nonce"
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(input.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double
)

@Service
class BlockchainService(
    @Value("\${node.address}") private val nodeAddress: String,
    private val restTemplate: RestTemplate
) {
    final val blockchain = mutableListOf<Block>()
    private val currentTransactions = ArrayList<Transaction>()
    private val difficulty = 4

    init {
        blockchain.add(Block(0, "1", System.currentTimeMillis(), listOf()))
    }

    fun addTransaction(sender: String, recipient: String, amount: Double) {
        currentTransactions.add(Transaction(sender, recipient, amount))
    }

    fun mineBlock(): Block {
        val lastBlock = blockchain.last()
        val newBlock =
            Block(blockchain.size, lastBlock.hash, System.currentTimeMillis(), ArrayList(currentTransactions))
        while (true) {
            newBlock.hash = newBlock.calculateHash()
            if (newBlock.hash.startsWith("0".repeat(difficulty))) {
                break
            }
            newBlock.nonce++
        }
        blockchain.add(newBlock)
        currentTransactions.clear()
        return newBlock
    }

    fun broadcastNewBlock(block: Block) {
        val nodeUrls = listOf("http://localhost:8081", "http://localhost:8082")
        nodeUrls.forEach {
            restTemplate.exchange("$it/blockchain/blocks", HttpMethod.POST, HttpEntity(block), String::class.java)
        }
    }

    @PostConstruct
    fun init() {
        val blockchainData = restTemplate.getForObject("$nodeAddress/blockchain", Array<Block>::class.java)
        if (blockchainData != null) {
            blockchain.clear()
            blockchain.addAll(blockchainData)
        }
    }
}

@Configuration
class AppConfig {
    @Bean
    fun restTemplate(): RestTemplate =
        RestTemplate()
}
