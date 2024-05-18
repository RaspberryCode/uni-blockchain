package pl.softwarekitchen.uni.blockchain.miner

import org.springframework.stereotype.Service
import pl.softwarekitchen.uni.blockchain.common.blockchain.Block
import pl.softwarekitchen.uni.blockchain.common.blockchain.BlockchainService
import pl.softwarekitchen.uni.blockchain.common.node.NodeService
import pl.softwarekitchen.uni.blockchain.common.transaction.RemoveTransaction
import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction
import pl.softwarekitchen.uni.blockchain.common.transaction.TransactionService
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

@Service
class MiningService(
    private val blockchainService: BlockchainService,
    private val nodeService: NodeService
) : TransactionService {
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

    override fun register(transaction: Transaction) {
        if (!blockchainService.isTransactionInBlockchain(transaction)
            && !isTransactionInMemPool(transaction)) {
            memPool.add(transaction)
        }
    }

    override fun getTransactions(): List<Transaction> =
        memPool.toList()

    override fun removeTransactions(transactions: List<Transaction>) {
        transactions.forEach { memPool.remove(it) }
    }

    private fun mineBlock() {
        val transactions = takeTransactionsFromMemPool()
        val validTransactions = gatherValidTransactions(transactions)

        if (validTransactions.isNotEmpty()) {
            val newBlock = prepareBlock(transactions)
            mineBlock(newBlock)
            blockchainService.processBlock(newBlock)
        }
        val invalidTransactions = transactions - validTransactions.toSet()
        if (invalidTransactions.isNotEmpty()) {
            nodeService.broadcast(RemoveTransaction(invalidTransactions))
        }
    }

    private fun gatherValidTransactions(transactions: List<Transaction>): List<Transaction> {
        val validTransactions = mutableListOf<Transaction>()
        val usersContext = mutableMapOf<String, Double>()
        for (transaction in transactions) {
            val (sender, recipient) = transaction
            val senderBalance = usersContext.getOrDefault(sender, blockchainService.getUserBalance(sender))
            if (senderBalance >= transaction.amount) {
                validTransactions.add(transaction)
                usersContext[sender] = senderBalance - transaction.amount
                usersContext[transaction.recipient] =
                    usersContext.getOrDefault(recipient, blockchainService.getUserBalance(sender)) + transaction.amount
            }
        }
        return validTransactions
    }

    private fun mineBlock(newBlock: Block) {
        while (!isValidProof(newBlock)) {
            newBlock.nonce++
            newBlock.hash = newBlock.calculateBlockHash()
        }
    }

    private fun prepareBlock(transactions: List<Transaction>): Block {
        val previousBlock = blockchainService.getBlockchain().lastOrNull()
        val previousHash = previousBlock?.hash ?: "0"
        val newBlock = Block(
            index = blockchainService.getBlockchain().size,
            previousHash = previousHash,
            timestamp = System.currentTimeMillis(),
            transactions = transactions
        )
        return newBlock
    }

    private fun takeTransactionsFromMemPool(): MutableList<Transaction> {
        val transactions = mutableListOf<Transaction>()
        while (memPool.isNotEmpty()) {
            transactions.add(memPool.poll())
        }
        return transactions
    }

    private fun isValidProof(block: Block): Boolean {
        return block.hash.startsWith("0".repeat(4))
    }

    private fun isTransactionInMemPool(transaction: Transaction): Boolean {
        return memPool.contains(transaction)
    }
}