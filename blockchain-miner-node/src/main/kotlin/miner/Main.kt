package miner

import java.util.*
import java.io.*
import java.net.*
import kotlin.collections.ArrayList
import java.security.MessageDigest

fun main() {
    val blockchain = Blockchain()
    val port = System.getProperty("port")?.toInt() ?: 5000  // Default to port 5000 if not specified
    val miner = Miner(blockchain, UUID.randomUUID().toString(), port)
    miner.start()
}

class Miner(
    private val blockchain: Blockchain,
    private val nodeIdentifier: String,
    private val port: Int
) {

    fun start() {
        val serverSocket = ServerSocket(port)
        println("Miner $nodeIdentifier started on port ${serverSocket.localPort}")
        while (true) {
            val clientSocket = serverSocket.accept()
            handleClient(clientSocket)
        }
    }

    private fun handleClient(clientSocket: Socket) {
        val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val writer = PrintWriter(clientSocket.getOutputStream(), true)

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val data = line!!.split(",")
            if (data.size == 3) {
                blockchain.addTransaction(data[0], data[1], data[2].toDouble())
                val newBlock = blockchain.mineBlock()
                println("Mined a new block: ${newBlock.hash}")
            }
            writer.println("Received transaction: $line")
        }
        clientSocket.close()
    }
}

class Blockchain {
    private val chain = mutableListOf<Block>()
    private val currentTransactions = ArrayList<Transaction>()
    private var difficulty = 4

    init {
        addBlock(Block(0, "1", System.currentTimeMillis(), listOf()))
    }

    fun addTransaction(sender: String, recipient: String, amount: Double): Int {
        currentTransactions.add(Transaction(sender, recipient, amount))
        return lastBlock().index + 1
    }

    fun mineBlock(): Block {
        val newBlock = Block(chain.size, lastBlock().hash, System.currentTimeMillis(), ArrayList(currentTransactions))
        while (true) {
            newBlock.hash = newBlock.calculateHash()
            if (newBlock.hash.startsWith("0".repeat(difficulty))) {
                break
            }
            newBlock.nonce++
        }
        addBlock(newBlock)
        currentTransactions.clear()
        return newBlock
    }

    private fun addBlock(newBlock: Block) {
        chain.add(newBlock)
    }

    private fun lastBlock(): Block =
        chain.last()
}

data class Block(
    val index: Int,
    val previousHash: String,
    val timestamp: Long,
    val transactions: List<Transaction>,
    var nonce: Int = 0,
    var hash: String = ""
) {
    fun calculateHash(): String {
        val input = previousHash + timestamp + transactions + nonce
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(input.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double
)