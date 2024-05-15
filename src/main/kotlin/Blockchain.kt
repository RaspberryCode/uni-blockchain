class Blockchain(difficulty: Int = 2) {
    private val blocks: MutableList<Block> = mutableListOf()
    private val validPrefix = "0".repeat(difficulty)
//  Unspent Transaction Output
    val utxo: MutableMap<String, TransactionOutput> = mutableMapOf()

    fun isValid(): Boolean {
        if (blocks.isEmpty()) return true
        if (blocks.size == 1) return blocks[0].hash == blocks[0].calculateHash()
        return blocks.zipWithNext().all { (prev, current) ->
            current.hash == current.calculateHash() &&
                    current.previousHash == prev.calculateHash() &&
                    isMined(prev) && isMined(current)
        }
    }

    fun add(block: Block): Block {
        val minedBlock = mine(block)
        blocks.add(minedBlock)
        updateUTXO(minedBlock)
        return minedBlock
    }

    private fun isMined(block: Block): Boolean = block.hash.startsWith(validPrefix)

    private fun mine(block: Block): Block {
        var minedBlock = block.copy()
        while (!isMined(minedBlock)) {
            minedBlock = minedBlock.copy(nonce = minedBlock.nonce + 1)
            minedBlock.hash = minedBlock.calculateHash()
        }
        return minedBlock
    }

    private fun updateUTXO(block: Block) {
        block.transactions.flatMap { it.inputs }.map { it.hash }.forEach { utxo.remove(it) }
        utxo.putAll(block.transactions.flatMap { it.outputs }.associateBy { it.hash })
    }

    fun latestHash(): String = blocks.last().hash
}