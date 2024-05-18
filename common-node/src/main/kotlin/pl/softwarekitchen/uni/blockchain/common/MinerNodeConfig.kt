package pl.softwarekitchen.uni.blockchain.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "node")
data class NodeConfiguration(
    var address: String,
    var initialPeers: List<String> = mutableListOf(),
    var initialBlockDifficulty: Int = 4
)

@Configuration
@EnableConfigurationProperties(NodeConfiguration::class)
class CommonConfiguration