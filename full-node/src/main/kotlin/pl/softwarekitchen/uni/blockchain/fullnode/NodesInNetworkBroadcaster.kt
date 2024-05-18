package pl.softwarekitchen.uni.blockchain.fullnode

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.softwarekitchen.uni.blockchain.common.node.NodeService

@Component
class NodesInNetworkBroadcaster(
    private val nodeService: NodeService,
) {
    private val logger: Logger = LoggerFactory.getLogger(NodesInNetworkBroadcaster::class.java)

    @Scheduled(fixedRate = 20000)
    fun broadcastKnownNodesToNetwork() {
        logger.info("Broadcasting known nodes to network")
        nodeService.getNodes()
            .forEach {
                nodeService.broadcast(it)
            }
    }
}
