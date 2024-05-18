package pl.softwarekitchen.uni.blockchain.user

import org.springframework.stereotype.Service
import pl.softwarekitchen.uni.blockchain.common.node.NodeService
import pl.softwarekitchen.uni.blockchain.common.transaction.Transaction
import pl.softwarekitchen.uni.blockchain.user.api.TransactionRequest

@Service
class UserTransactionService(
    private val nodeService: NodeService
) {
    fun sendTransaction(user: BlockchainUser, request: TransactionRequest) {
        nodeService.broadcast(
            Transaction(
                sender = user.username,
                recipient = request.recipient,
                amount = request.amount,
                timestamp = System.currentTimeMillis(),
                key = user.key
            )
        )
    }
}