package pl.softwarekitchen.uni.blockchain.user

import org.springframework.stereotype.Service
import pl.softwarekitchen.uni.blockchain.common.blockchain.BlockchainService

@Service
class UserWalletService(
    private val blockchainService: BlockchainService
) {
    fun getWalletBalance(targetUsername: String): Double {
        return blockchainService.getBlockchain()
            .flatMap { it.transactions }
            .filter { it.recipient == targetUsername || it.sender == targetUsername }
            .fold(0.0) { acc, transaction ->
                when {
                    transaction.recipient == targetUsername -> acc + transaction.amount
                    transaction.sender == targetUsername    -> acc - transaction.amount
                    else                                    -> acc
                }
            }
    }
}