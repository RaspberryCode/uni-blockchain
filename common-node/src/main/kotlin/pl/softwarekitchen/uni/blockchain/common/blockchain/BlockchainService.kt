package pl.softwarekitchen.uni.blockchain.common.blockchain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pl.softwarekitchen.uni.blockchain.common.NodeConfiguration
import pl.softwarekitchen.uni.blockchain.common.node.NodeService
import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction

@Service
class BlockchainService(
    private val nodeConfiguration: NodeConfiguration,
    private val nodeService: NodeService
) {
    private val blockchain = mutableListOf(
        genesisBlock(nodeConfiguration.initialBlockDifficulty)
    )
    private val logger: Logger = LoggerFactory.getLogger(BlockchainService::class.java)


    fun processBlock(block: Block) {
        if (validateAndAdd(block)) {
            nodeService.broadcast(block)
        }
    }

    fun getBlockchain() =
        blockchain

    fun isTransactionInBlockchain(transaction: Transaction): Boolean {
        return blockchain.any { block ->
            block.transactions.any { it == transaction }
        }
    }

    fun getUserBalance(user: String): Double {
        var balance = 0.0
        blockchain.forEach { block ->
            block.transactions.forEach { tx ->
                if (tx.sender == user) {
                    balance -= tx.amount
                }
                if (tx.recipient == user) {
                    balance += tx.amount
                }
            }
        }
        return balance
    }

    @Synchronized
    private fun validateAndAdd(block: Block): Boolean {
        if (existsOnBlockchain(block)) {
            logger.warn("Block already exists on blockchain")
            return false
        }
        if (blockchain.isNotEmpty()
            && !isValidBlock(block, blockchain.last())
        ) {
            logger.warn("Invalid block")
            return false
        }

        blockchain.add(block)
        return true
    }

    private fun existsOnBlockchain(block: Block) =
        blockchain.any { it.hash == block.hash }

    private fun isValidBlock(newBlock: Block, previousBlock: Block): Boolean {
        if (previousBlock.hash != newBlock.previousHash) return false
        if (!isValidProof(newBlock)) return false
        return true
    }

    private fun isValidProof(block: Block): Boolean {
        return block.hash
            .startsWith("0".repeat(nodeConfiguration.initialBlockDifficulty))
                && block.hash == block.calculateBlockHash()
    }
}