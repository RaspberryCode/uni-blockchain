package pl.softwarekitchen.uni.blockchain.common.node.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.softwarekitchen.uni.blockchain.common.node.NodeService

@RestController
@RequestMapping("/nodes")
class NodeController(val nodeService: NodeService) {

    @PostMapping
    fun registerNode(@RequestBody nodeAddress: String){
        nodeService.registerNode(nodeAddress)
    }

    @GetMapping
    fun getNodes(): Collection<String> =
        nodeService.getNodes()
}