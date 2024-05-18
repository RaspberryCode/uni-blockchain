package pl.softwarekitchen.uni.blockchain.common.blockchain;

import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction

fun genesisBlock(difficulty: Int): Block {
    val genesisBlock = Block(
        index = 0,
        previousHash = "0",
        timestamp = 1716007557532,
        nonce = 39731,
        hash = "0000f342401b41d10c518dbf18b18d26934d12e94eea70502dc08e9f3c69ed00",
        transactions = listOf(
            Transaction(
                sender = "",
                recipient = "user1",
                amount = 2000.0,
                signature = "GENESIS",
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                sender = "",
                recipient = "user2",
                amount = 1000.0,
                signature = "GENESIS",
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                sender = "",
                recipient = "user3",
                amount = 500.0,
                signature = "GENESIS",
                timestamp = System.currentTimeMillis()
            ),
        )
    )
    mine(genesisBlock, difficulty)
    return genesisBlock
}

private fun mine(newBlock: Block, difficulty: Int) {
    while (!isValidProof(newBlock, difficulty)) {
        newBlock.nonce++
        newBlock.hash = newBlock.calculateBlockHash()
    }
}

private fun isValidProof(block: Block, difficulty: Int): Boolean {
    return block.hash.startsWith("0".repeat(difficulty))
}