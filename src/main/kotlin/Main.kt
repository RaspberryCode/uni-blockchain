fun main() {
    val startTime = System.currentTimeMillis()

    val blockChain = Blockchain(3)
    val wallet1 = Wallet.create(blockChain)
    val wallet2 = Wallet.create(blockChain)

    println("Wallet 1 balance: ${wallet1.balance}")
    println("Wallet 2 balance: ${wallet2.balance}")

    // Creating a transaction
    val tx1 = wallet1.createInitialTransaction(100)
    blockChain.add(Block("0").addTransaction(tx1))

    println("Blockchain valid post-genesis: ${blockChain.isValid()}")

    println("Wallet 1 balance: ${wallet1.balance}")
    println("Wallet 2 balance: ${wallet2.balance}")

    val tx2 = wallet1.sendFundsTo(wallet2.publicKey, 33)
    blockChain.add(Block(blockChain.latestHash()).addTransaction(tx2))

    println("Wallet 1 balance: ${wallet1.balance}")
    println("Wallet 2 balance: ${wallet2.balance}")

    println("Blockchain valid after all transactions: ${blockChain.isValid()}")

    val endTime = System.currentTimeMillis()
    println("Execution Time: ${endTime - startTime} ms")
}