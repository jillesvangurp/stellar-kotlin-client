package io.inbot.kotlinstellar

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.describe
import org.stellar.sdk.responses.operations.PaymentOperationResponse

class TestnetTest {
    lateinit var server: Server
    lateinit var wrapper: KotlinStellarWrapper

    @BeforeEach
    fun before() {
        server = Server("https://horizon-testnet.stellar.org")
        wrapper = KotlinStellarWrapper(server, stellarNetwork = StellarNetwork.testnet)
    }

    @Test
    fun dumpPayments() {
        wrapper.paymentSequence(
            account = KeyPair.fromAccountId("GC24FTHW24Z2XP6FYQVD55XBKKRB4MM7P4NJEDJGL75LBNDNIGMP4J3N"),
            cursor = "0"
        ).forEach {
            val transaction = wrapper.server.transactions().transaction(it.transactionHash)
            println("${it.from.accountId} ${it.amount} ${it.asset.describe()} tx: ${it.transactionHash} ledger: ${transaction.ledger}")
            wrapper.server.operations().forTransaction(it.transactionHash).execute().records.forEach {
                val por = it as PaymentOperationResponse
                println("\t${por.from.accountId} ${por.amount} ${por.asset.describe()} ${por.to.accountId}")
            }
        }
    }
}