package pl.softwarekitchen.uni.blockchain.common.blockchain

import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction
import java.security.MessageDigest

data class Block(
    val index: Int,
    val previousHash: String,
    val timestamp: Long,
    val transactions: List<Transaction>,
    var nonce: Int = 0,
    var hash: String = ""
){
    fun calculateBlockHash(): String {
        val input = "${previousHash}${timestamp}${transactions}${nonce}"
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("")
            { str, it -> str + "%02x".format(it) }
    }
}