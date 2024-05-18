package pl.softwarekitchen.uni.blockchain.user.api

data class TransactionRequest(val recipient: String, val amount: Double)