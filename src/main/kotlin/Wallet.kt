import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

data class Wallet(val publicKey: PublicKey, val privateKey: PrivateKey, val blockChain: Blockchain) {
    companion object {
        fun create(blockChain: Blockchain): Wallet {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            val keyPair = generator.generateKeyPair()
            return Wallet(keyPair.public, keyPair.private, blockChain)
        }
    }

    val balance: Int
        get() = blockChain.utxo.filterValues { it.isMine(publicKey) }.values.sumOf { it.amount }

    fun createInitialTransaction(amount: Int): Transaction {
        val tx = Transaction.create(publicKey, publicKey, amount)
        tx.outputs.add(TransactionOutput(publicKey, amount, tx.hash))
        tx.sign(privateKey)
        return tx
    }

    fun sendFundsTo(recipient: PublicKey, amountToSend: Int): Transaction {
        val tx = Transaction.create(publicKey, recipient, amountToSend)
        val myTransactions = blockChain.utxo.filterValues { it.isMine(publicKey) }.values
        val collectedOutputs = mutableListOf<TransactionOutput>()

        var collectedAmount = 0
        for (output in myTransactions) {
            if (collectedAmount >= amountToSend) break
            collectedOutputs.add(output)
            collectedAmount += output.amount
        }

        tx.inputs.addAll(collectedOutputs)
        tx.outputs.add(TransactionOutput(recipient, amountToSend, tx.hash))

        if (collectedAmount > amountToSend) {
            tx.outputs.add(TransactionOutput(publicKey, collectedAmount - amountToSend, tx.hash))
        }

        tx.sign(privateKey)
        return tx
    }
}