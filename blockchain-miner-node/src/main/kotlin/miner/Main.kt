package miner

import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.annotation.Retry
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

@SpringBootApplication
@EnableConfigurationProperties(MinerNodeConfig::class)
class MinerApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

fun main(args: Array<String>) {
    runApplication<MinerApplication>(*args)
}

@Configuration
class ResilienceConfig {
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

@ConfigurationProperties(prefix = "minernode")
data class MinerNodeConfig(
    var currentAddress: String,
    var peers: List<String> = mutableListOf()
)

@RestController
class Controller(
    val blockchainService: BlockchainService,
    val nodeService: NodeService,
    val miningService: MiningService
) {

    @PostMapping("/transactions")
    fun newTransaction(@RequestBody transaction: Transaction): String {
        miningService.register(transaction)
        return "Transaction added"
    }

    @PostMapping("/node")
    fun registerNode(@RequestBody nodeAddress: String): ResponseEntity<String> {
        if (nodeService.checkIfExistsAndAdd(nodeAddress)) {
            nodeService.broadcast(nodeAddress)
        }
        return ResponseEntity.ok("Node registered successfully")
    }

    @PostMapping("/block")
    fun receiveBlock(@RequestBody block: Block): ResponseEntity<String> {
        if (blockchainService.validateAndAdd(block)) {
            nodeService.broadcast(block)
            return ResponseEntity.ok("Block added successfully")
        }
        return ResponseEntity.badRequest().body("Invalid block")
    }

    @GetMapping("/blockchain")
    fun getBlockchain() =
        blockchainService.getBlockchain()

    @GetMapping("/nodes")
    fun getNodes() =
        nodeService.getNodes()
}

@Service
class BlockchainService {
    private val blockchain = mutableListOf<Block>()

    private val difficulty = 4

    @Synchronized
    fun validateAndAdd(block: Block): Boolean {
        if (blockchain.isNotEmpty()
            && !isValidBlock(block, blockchain.last())
        ) {
            println("Invalid block")
            return false
        }
        if (existsOnBlockchain(block)) {
            println("Block already exists on blockchain")
            return true
        }

        blockchain.add(block)
        return true
    }

    fun getBlockchain() =
        blockchain

    private fun existsOnBlockchain(block: Block) =
        blockchain.any { it.hash == block.hash }

    private fun isValidBlock(newBlock: Block, previousBlock: Block): Boolean {
        if (previousBlock.hash != newBlock.previousHash) return false
        if (!isValidProof(newBlock)) return false
        return true
    }

    private fun isValidProof(block: Block): Boolean {
        return block.hash.startsWith("0".repeat(difficulty)) && block.hash == calculateBlockHash(block)
    }

    companion object {
        fun calculateBlockHash(block: Block): String {
            val input = "${block.previousHash}${block.timestamp}${block.transactions}${block.nonce}"
            return MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray())
                .fold("")
                { str, it -> str + "%02x".format(it) }
        }
    }
}

@Service
class NodeService(
    private val restTemplate: RestTemplate,
    private val minerNodeConfig: MinerNodeConfig
) {
    private val nodes = ConcurrentHashMap.newKeySet<String>()
    private val nodeFailures = ConcurrentHashMap<String, NodeFailureInfo>()
    private final val timeToStoreFailures = 10_000

    @PostConstruct
    fun init() {
        minerNodeConfig.peers
            .forEach { peerAddress ->
                println("Trying to register with peer $peerAddress")
                try {
                    restTemplate.getForEntity("$peerAddress/actuator/health", String::class.java)
                    checkIfExistsAndAdd(peerAddress)
                    informNodeAboutNewNode(peerAddress, minerNodeConfig.currentAddress)
                } catch (e: Exception) {
                    println("Failed to register with peer $peerAddress")
                }
            }
    }

    @Synchronized
    fun checkIfExistsAndAdd(nodeAddress: String): Boolean {
        if (!nodes.contains(nodeAddress) && nodeAddress != minerNodeConfig.currentAddress) {
            nodes.add(nodeAddress)
            println("Node $nodeAddress added successfully")
            return true
        }
        return false
    }

    fun broadcast(block: Block) {
        nodes.forEach { address ->
            informNodeAboutNewBlock(address, block)
        }
    }

    @Retry(name = "postBlock", fallbackMethod = "trackBlockFailure")
    private fun informNodeAboutNewBlock(address: String, block: Block) {
        restTemplate.postForObject("$address/block", block, String::class.java)
    }

    fun getNodes() =
        nodes

    fun broadcast(nodeAddress: String) {
        nodes.filter { it != nodeAddress }
            .forEach { address ->
                informNodeAboutNewNode(address, nodeAddress)
            }
    }

    @Retry(name = "registerNode", fallbackMethod = "trackNodeFailure")
    private fun informNodeAboutNewNode(address: String, nodeAddress: String) {
        restTemplate.postForObject("$address/node", nodeAddress, String::class.java)
    }

    @Suppress("unused")
    private fun trackBlockFailure(address: String, block: Block, e: Exception) {
        trackFailure(address)
    }

    @Suppress("unused")
    private fun trackNodeFailure(address: String, nodeAddress: String, e: Exception) {
        trackFailure(address)
    }

    private fun trackFailure(address: String) {
        val now = System.currentTimeMillis()
        nodeFailures.compute(address) { _, failureInfo ->
            val info = failureInfo ?: NodeFailureInfo()
            info.failures.add(now)
            info.failures.removeIf { it < now - timeToStoreFailures }
            if (info.failures.size >= 3) {
                removeNode(address)
            }
            info
        }
    }

    @Synchronized
    private fun removeNode(address: String) {
        nodes.remove(address)
        nodeFailures.remove(address)
        println("Node $address removed due to multiple failures")
    }

    data class NodeFailureInfo(
        val failures: MutableList<Long> = mutableListOf()
    )
}

data class Block(
    val index: Int,
    val previousHash: String,
    val timestamp: Long,
    val transactions: List<Transaction>,
    var nonce: Int = 0,
    var hash: String = ""
)

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double,
    val signature: String
)

@Service
class MiningService(
    private val blockchainService: BlockchainService,
    private val nodeService: NodeService
) {
    private val memPool = ConcurrentLinkedQueue<Transaction>()

    init {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            while (true) {
                mineBlock()
                Thread.sleep(1000)
            }
        }
    }
    fun register(transaction: Transaction) {
        memPool.add(transaction)
    }
    private fun mineBlock() {
        val transactions = mutableListOf<Transaction>()
        while (memPool.isNotEmpty()) {
            transactions.add(memPool.poll())
        }

        if (transactions.isNotEmpty()) {
            val previousBlock = blockchainService.getBlockchain().lastOrNull()
            val previousHash = previousBlock?.hash ?: "0"
            val newBlock = Block(
                index = blockchainService.getBlockchain().size,
                previousHash = previousHash,
                timestamp = System.currentTimeMillis(),
                transactions = transactions
            )

            // Mining process
            while (!isValidProof(newBlock)) {
                newBlock.nonce++
                newBlock.hash = BlockchainService.calculateBlockHash(newBlock)
            }

            if (blockchainService.validateAndAdd(newBlock)) {
                nodeService.broadcast(newBlock)
                println("New block mined and broadcasted: \n$newBlock")
            }
        }
    }

    private fun isValidProof(block: Block): Boolean {
        return block.hash.startsWith("0".repeat(4))
    }
}