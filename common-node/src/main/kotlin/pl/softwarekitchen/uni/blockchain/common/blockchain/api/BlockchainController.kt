package pl.softwarekitchen.uni.blockchain.common.blockchain.api

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pl.softwarekitchen.uni.blockchain.common.blockchain.Block
import pl.softwarekitchen.uni.blockchain.common.blockchain.BlockchainService
import pl.softwarekitchen.uni.blockchain.common.transaction.TransactionService

@RestController
@ConditionalOnBean(TransactionService::class)
class BlockchainController(
    val blockchainService: BlockchainService,
    val transactionService: TransactionService
) {

    @PostMapping("/block")
    fun receiveBlock(@RequestBody block: Block){
        blockchainService.processBlock(block)
        transactionService.removeTransactions(block.transactions)
    }

    @GetMapping("/blockchain")
    fun getBlockchain() =
        blockchainService.getBlockchain()
}