package pl.softwarekitchen.uni.blockchain.user.api

import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.softwarekitchen.uni.blockchain.user.BlockchainUser
import pl.softwarekitchen.uni.blockchain.user.UserTransactionService
import pl.softwarekitchen.uni.blockchain.user.UserWalletService
import java.util.*

@RestController
@RequestMapping("/user")
class UserController(
    val userTransactionService: UserTransactionService,
    val userWalletService: UserWalletService
) {
    private val users = listOf(
        BlockchainUser("user1", "password1", Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded),
        BlockchainUser("user2", "password2", Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded),
        BlockchainUser("user3", "password3", Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded)
    )

    @PostMapping("/transactions")
    fun createTransaction(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: TransactionRequest
    ): ResponseEntity<String> {
        val (username, password) = decodeBasicAuth(authHeader)
        val user = authenticate(username, password)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")

        userTransactionService.sendTransaction(user, request)
        return ResponseEntity.ok("Transaction created and broadcasted")
    }

    @GetMapping("/wallet")
    fun getWalletState(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Double> {
        val (username, _) = decodeBasicAuth(authHeader)

        return ResponseEntity.ok(userWalletService.getWalletBalance(username))
    }

    @GetMapping("/wallet/{targetUsername}")
    fun getWalletState(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable targetUsername: String
    ): Double {
        return userWalletService.getWalletBalance(targetUsername)
    }

    private fun decodeBasicAuth(authHeader: String): Pair<String, String> {
        val base64Credentials = authHeader.substringAfter("Basic ")
        val credentials = String(Base64.getDecoder().decode(base64Credentials)).split(":")
        return Pair(credentials[0], credentials[1])
    }

    private fun authenticate(username: String, password: String): BlockchainUser? {
        return users.find { it.username == username && it.password == password }
    }
}