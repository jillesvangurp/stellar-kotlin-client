package io.inbot.ethclient.stellar

import assertk.assert
import assertk.assertions.isEqualTo
import io.inbot.kotlinstellar.KotlinStellarWrapper
import io.inbot.kotlinstellar.balanceAmount
import io.inbot.kotlinstellar.balanceFor
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse

private val logger = KotlinLogging.logger { }

class StellarTest {
    lateinit var server: Server
    lateinit var wrapper: KotlinStellarWrapper

    @BeforeEach
    fun before() {
        server = Server("http://localhost:8000")
        wrapper = KotlinStellarWrapper(server)
    }

    @Test
    fun `should recreate same account from seed`() {
        val pair = KeyPair.random()

        val secretSeed = pair.secretSeed
        logger.info("secret = ${secretSeed.joinToString("")} ${pair.accountId}")

        val secondPair = KeyPair.fromSecretSeed(secretSeed)

        assert(pair.accountId).isEqualTo(secondPair.accountId)
    }

    @Test
    fun `use standalone root to create a new account`() {
        // this is the minimum on the standalone network; should be lower on the public stellars
        val amountLumen = 20.0
        val newKeyPair = wrapper.createNewAccount(amountLumen)

        try {
            val newAccount = server.accounts().account(newKeyPair)
            assert(newAccount.balances[0].balance).equals("${amountLumen}000000")
        } catch (e: ErrorResponse) {
            logger.info("${e.code} ${e.body}")
            throw e
        }
    }

    @Test
    fun `browny points as a token`() {
        val brownyPointIssuer = wrapper.createNewAccount(100.0, "issuer")
        val bpAss = Asset.createNonNativeAsset("BrownyPoints", brownyPointIssuer)

        // need larger opening balance for creating trust
        val anotherAccount = wrapper.createNewAccount(100.0, "receiver")

        wrapper.trustAsset(anotherAccount, bpAss, 100.0)

        wrapper.pay(bpAss, brownyPointIssuer, anotherAccount, 2.0)

        assert(server.accounts().account(anotherAccount).balanceFor(bpAss)?.balanceAmount()).isEqualTo(2.0)
    }

    @Test
    fun `trading with and without trust lines`() {
        val brownyPointIssuer = wrapper.createNewAccount(100.0, "issuer")
        val bpAss = Asset.createNonNativeAsset("BrownyPoints", brownyPointIssuer)

        val pairs = mutableListOf<KeyPair>()
        for (i in 1..3) {
            pairs.add(wrapper.createNewAccount(100.0, "receiver"))
        }

        pairs.forEach {
            wrapper.trustAsset(it, bpAss, 1000.0)
            wrapper.pay(bpAss, brownyPointIssuer, it, 100.0)
        }
        logger.info("after")
    }

    @Test
    fun `coroutines test`() {
        logger.info("in the beginning")
        runBlocking {
            awaitAll(async {
                delay(30L)
                logger.info("1")
            },
                async {
                    delay(30L)
                    logger.info("2")
                })
        }
        logger.info("last")
    }
}