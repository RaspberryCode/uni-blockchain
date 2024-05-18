package pl.softwarekitchen.uni.blockchain.common.transaction

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double,
    val signature: String,
    val timestamp: Long
) {
    constructor(sender: String, recipient: String, amount: Double, key: ByteArray, timestamp: Long) :
            this(sender, recipient, amount, signTransaction(sender, recipient, amount, key, timestamp), timestamp)

    companion object {
        private fun signTransaction(
            sender: String,
            recipient: String,
            amount: Double,
            key: ByteArray,
            timestamp: Long
        ): String {
            val signaturePayload = "${sender}:${recipient}:${amount}:${timestamp}"
            return Jwts.builder()
                .setSubject(signaturePayload)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact()
        }
    }
}