package pl.softwarekitchen.uni.blockchain.common.transaction.api

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.softwarekitchen.uni.blockchain.common.transaction.RemoveTransaction
import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction
import pl.softwarekitchen.uni.blockchain.common.transaction.TransactionService

@RestController
@RequestMapping("/transactions")
@ConditionalOnBean(TransactionService::class)
class TransactionController(val transactionService: TransactionService) {
    @DeleteMapping
    fun removeTransactions(@RequestBody removeTransactionCommand: RemoveTransaction): String {
        transactionService.removeTransactions(removeTransactionCommand.invalidTransactions)
        return "Transactions removed"
    }

    @PostMapping
    fun newTransaction(@RequestBody transaction: Transaction): String {
        transactionService.register(transaction)
        return "Transaction added"
    }

    @GetMapping
    fun getTransactions(): List<Transaction> =
        transactionService.getTransactions()
}