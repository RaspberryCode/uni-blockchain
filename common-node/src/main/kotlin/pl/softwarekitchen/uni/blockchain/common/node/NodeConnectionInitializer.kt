package pl.softwarekitchen.uni.blockchain.common.node

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import pl.softwarekitchen.uni.blockchain.common.NodeConfiguration
import pl.softwarekitchen.uni.blockchain.common.blockchain.Block
import pl.softwarekitchen.uni.blockchain.common.blockchain.BlockchainService

@Component
class NodeConnectionInitializer(
    private val nodeService: NodeService,
    private val restTemplate: RestTemplate,
    private val nodeConfig: NodeConfiguration,
    private val blockchainService: BlockchainService
) {
    private val logger: Logger = LoggerFactory.getLogger(NodeConnectionInitializer::class.java)

    @PostConstruct
    fun init() {
        nodeConfig.initialPeers
            .filter { it != nodeConfig.address }
            .forEach { peerAddress ->
                logger.info("Trying to register with peer $peerAddress")
                tryConnectingToNode(peerAddress)
            }
    }

    @Scheduled(fixedRate = 10000)
    fun ensureConnectionToOriginalPeers() {
        nodeConfig.initialPeers
            .filter { it != nodeConfig.address }
            .forEach { peerAddress ->
                tryConnectingToNode(peerAddress)
            }
    }

    private fun tryConnectingToNode(peerAddress: String) {
        try {
            restTemplate.getForEntity("$peerAddress/actuator/health", String::class.java)
            nodeService.checkIfExistsAndAdd(peerAddress)
            initializeBlockchain(peerAddress)
            nodeService.informAboutNewNode(peerAddress, nodeConfig.address)
        } catch (e: Exception) {
            logger.debug("Failed to register with peer $peerAddress")
        }
    }

    private fun initializeBlockchain(sourceNode: String) {
        if (blockchainService.getBlockchain().isEmpty()) {
            logger.debug("Initializing blockchain from $sourceNode")
            restTemplate.getForEntity<List<Block>>(
                "$sourceNode/blockchain",
                List::class.java
            ).body?.forEach { blockchainService.processBlock(it) }
        }
    }
}
