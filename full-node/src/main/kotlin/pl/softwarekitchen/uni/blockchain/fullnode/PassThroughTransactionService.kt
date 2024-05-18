package pl.softwarekitchen.uni.blockchain.fullnode

import org.springframework.stereotype.Service
import pl.softwarekitchen.uni.blockchain.common.blockchain.BlockchainService
import pl.softwarekitchen.uni.blockchain.common.node.NodeService
import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction
import pl.softwarekitchen.uni.blockchain.common.transaction.TransactionService
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class PassThroughTransactionService(
    private val blockchainService: BlockchainService,
    private val nodeService: NodeService
) : TransactionService {
    private val memPool = ConcurrentLinkedQueue<Transaction>()

    override fun register(transaction: Transaction) {
        if (!blockchainService.isTransactionInBlockchain(transaction)
            && !isTransactionInMemPool(transaction)) {
            memPool.add(transaction)
            nodeService.broadcast(transaction)
        }
    }

    override fun getTransactions(): List<Transaction> =
        memPool.toList()

    override fun removeTransactions(transactions: List<Transaction>) {
        transactions.forEach { memPool.remove(it) }
    }

    private fun isTransactionInMemPool(transaction: Transaction): Boolean {
        return memPool.contains(transaction)
    }
}