package pl.softwarekitchen.uni.blockchain.common.node

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pl.softwarekitchen.uni.blockchain.common.NodeConfiguration
import pl.softwarekitchen.uni.blockchain.common.blockchain.Block
import pl.softwarekitchen.uni.blockchain.common.transaction.RemoveTransaction
import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction
import java.util.concurrent.ConcurrentHashMap

@Service
class NodeService(
    private val restTemplate: RestTemplate,
    private val nodeConfig: NodeConfiguration
) {
    private val nodes = ConcurrentHashMap.newKeySet<String>()
    private val nodeFailures = ConcurrentHashMap<String, NodeFailureInfo>()
    private final val timeToStoreFailures = 10_000
    private val logger: Logger = LoggerFactory.getLogger(NodeService::class.java)

    fun registerNode(nodeAddress: String) {
        if (nodeIsAlive(nodeAddress)) {
            if (checkIfExistsAndAdd(nodeAddress)) {
                logger.info("New node $nodeAddress added")
                broadcast(nodeAddress)
            } else {
                logger.debug("Node $nodeAddress already exists in network")
            }
        } else {
            logger.warn("Node $nodeAddress is not alive")
        }
    }

    private fun nodeIsAlive(nodeAddress: String): Boolean {
        return try {
            restTemplate.getForEntity("$nodeAddress/actuator/health", String::class.java)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getNodes(): Collection<String> =
        nodes


    fun broadcast(block: Block) {
        logger.info("Broadcasting new block to network")
        nodes.forEach { address ->
            informNodeAboutNewBlock(address, block)
        }
    }

    fun broadcast(transaction: Transaction) {
        logger.info("Broadcasting new transaction to network")
        nodes.forEach { address ->
            informNodeAboutNewTransaction(address, transaction)
        }
    }

    fun broadcast(removeTransactionCommand: RemoveTransaction) {
        logger.info("Broadcasting remove transaction command to network")
        nodes.forEach { address ->
            informNodeAboutInvalidTransactions(address, removeTransactionCommand)
        }
    }

    private fun informNodeAboutInvalidTransactions(address: String, removeTransactionCommand: RemoveTransaction) {
        val objectMapper = jacksonObjectMapper()
        val requestBody = objectMapper.writeValueAsString(removeTransactionCommand)

        println("Request body: $requestBody")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(requestBody, headers)

        restTemplate.exchange("$address/transactions", HttpMethod.DELETE, entity, String::class.java)
    }


    fun broadcast(nodeAddress: String) {
        logger.debug("Broadcasting node address $nodeAddress to network")
        nodes.filter { it != nodeAddress }
            .forEach { address ->
                informAboutNewNode(address, nodeAddress)
            }
    }

    @Retry(name = "postBlock", fallbackMethod = "trackBlockFailure")
    private fun informNodeAboutNewBlock(address: String, block: Block) {
        restTemplate.postForObject("$address/block", block, String::class.java)
    }

    @Retry(name = "postTransaction", fallbackMethod = "trackTransactionFailure")
    private fun informNodeAboutNewTransaction(address: String, transaction: Transaction) {
        restTemplate.postForObject("$address/transactions", transaction, String::class.java)
    }

    @Synchronized
    internal fun checkIfExistsAndAdd(nodeAddress: String): Boolean {
        if (!nodes.contains(nodeAddress) && nodeAddress != nodeConfig.address) {
            nodes.add(nodeAddress)
            logger.info("Node $nodeAddress added successfully")
            return true
        }
        return false
    }

    @Retry(name = "registerNode", fallbackMethod = "trackNodeFailure")
    internal fun informAboutNewNode(target: String, newNode: String) {
        restTemplate.postForObject("$target/nodes", newNode, String::class.java)
    }

    @Suppress("unused")
    private fun trackTransactionFailure(address: String, transaction: Transaction, e: Exception) {
        logger.warn("Transaction broadcast failed at $address for transaction $transaction")
        trackFailure(address)
    }

    @Suppress("unused")
    private fun trackBlockFailure(address: String, block: Block, e: Exception) {
        logger.warn("Block broadcast failed at $address for block $block")
        trackFailure(address)
    }

    @Suppress("unused")
    private fun trackNodeFailure(address: String, nodeAddress: String, e: Exception) {
        logger.warn("Node broadcast failed at $address for node $nodeAddress")
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
        logger.error("Node $address removed due to multiple failures")
    }

    data class NodeFailureInfo(
        val failures: MutableList<Long> = mutableListOf()
    )
}