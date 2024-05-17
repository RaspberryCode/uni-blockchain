package userapi

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableConfigurationProperties(FullNodeConfig::class)
class UserApiApplication {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

fun main(args: Array<String>) {
    runApplication<UserApiApplication>(*args)
}

data class BlockchainUser(val username: String, val password: String, val key: ByteArray)
data class TransactionRequest(val recipient: String, val amount: Double)

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Double,
    val signature: String
)

@Configuration
class SecurityConfig {

    @Bean
    fun userDetailsService(): UserDetailsService {
        val userDetailsList = listOf<UserDetails>(
                User.withDefaultPasswordEncoder().username("user1").password("password1").roles("USER").build(),
        User.withDefaultPasswordEncoder().username("user2").password("password2").roles("USER").build(),
        User.withDefaultPasswordEncoder().username("user3").password("password3").roles("USER").build()
        )
        return InMemoryUserDetailsManager(userDetailsList)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .authorizeHttpRequests { requests ->
                requests.anyRequest().authenticated()
            }
            .httpBasic()
        return http.build()
    }
}


@ConfigurationProperties(prefix = "userapi")
data class FullNodeConfig(
    var currentAddress: String,
    var peers: List<String> = mutableListOf()
)

@RestController
class UserController(val restTemplate: RestTemplate, val config: FullNodeConfig) {
    private val users = listOf(
        BlockchainUser("user1", "password1", Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded),
        BlockchainUser("user2", "password2", Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded),
        BlockchainUser("user3", "password3", Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded)
    )

    @PostMapping("/transaction")
    fun createTransaction(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: TransactionRequest
    ): ResponseEntity<String> {
        val (username, password) = decodeBasicAuth(authHeader)
        val user = authenticate(username, password) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            "Invalid credentials"
        )

        val transaction = Transaction(
            sender = username,
            recipient = request.recipient,
            amount = request.amount,
            signature = signTransaction(username, user.key, request)
        )

        config.peers.forEach { peer ->
            try {
                restTemplate.postForEntity("$peer/transactions", transaction, String::class.java)
                println("Broadcasted to $peer")
            } catch (e: Exception) {
                println("Failed to broadcast to $peer")
            }
        }
        return ResponseEntity.ok("Transaction created and broadcasted")
    }

    private fun decodeBasicAuth(authHeader: String): Pair<String, String> {
        val base64Credentials = authHeader.substringAfter("Basic ")
        val credentials = String(Base64.getDecoder().decode(base64Credentials)).split(":")
        return Pair(credentials[0], credentials[1])
    }

    private fun authenticate(username: String, password: String): BlockchainUser? {
        return users.find { it.username == username && it.password == password }
    }

    private fun signTransaction(sender: String, key: ByteArray, request: TransactionRequest): String {
        val signaturePayload = "${sender}:${request.recipient}:${request.amount}"
        return Jwts.builder()
            .setSubject(signaturePayload)
            .signWith(SignatureAlgorithm.HS256, key)
            .compact()
    }
}
