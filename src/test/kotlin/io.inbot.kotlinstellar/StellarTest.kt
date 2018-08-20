package io.inbot.ethclient.stellar

import io.inbot.kotlinstellar.KotlinStellarWrapper
import io.inbot.kotlinstellar.balanceAmount
import io.inbot.kotlinstellar.balanceFor
import io.kotlintest.matchers.string.contain
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import kotlinx.coroutines.experimental.Deferred
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

        pair.accountId shouldBe secondPair.accountId
    }

    @Test
    fun `use standalone root to create a new account`() {
        // this is the minimum on the standalone network; should be lower on the public stellars
        val amountLumen = 20.0
        val newKeyPair = wrapper.createNewAccount(amountLumen)

        try {
            val newAccount = server.accounts().account(newKeyPair)
            newAccount.balances[0].balance shouldBe "${amountLumen}000000"
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

        server.accounts().account(anotherAccount).balanceFor(bpAss)?.balanceAmount() shouldBe 2.0
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

        // bp holders can pay each other without further need for trustlines
        wrapper.pay(bpAss, pairs[0], pairs[1], 10.0)
        val anotherAccount = wrapper.createNewAccount(100.0)

        shouldThrow<IllegalStateException> {
            wrapper.pay(bpAss, pairs[2], anotherAccount, 10.0)
        }.message should (contain("op_no_trust"))
    }

    @Test
    fun `concurrent transaction should retry`() {
        runBlocking {
            val tasks = mutableListOf<Deferred<KeyPair>>()
            for(i in 0..2) {
                tasks.add(async {
                    wrapper.createNewAccount(20.0,maxTries = 20)
                })
            }
            val all = awaitAll(*tasks.toTypedArray())
            all.size shouldBe 3
        }
    }
}