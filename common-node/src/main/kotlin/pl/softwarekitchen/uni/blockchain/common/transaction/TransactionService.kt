package pl.softwarekitchen.uni.blockchain.common.transaction

interface TransactionService {
    fun register(transaction: Transaction)
    fun getTransactions(): List<Transaction>
    fun removeTransactions(transactions: List<Transaction>)
}