package io.inbot.ethclient.stellar

import io.inbot.kotlinstellar.KotlinStellarWrapper
import io.inbot.kotlinstellar.balanceAmount
import io.inbot.kotlinstellar.balanceFor
import io.inbot.kotlinstellar.findAccount
import io.inbot.kotlinstellar.toPublicPair
import io.kotlintest.matchers.string.contain
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse

private val logger = KotlinLogging.logger { }

// creating accounts is time consuming so try to reuse the same accounts and simply check if they exist before creating
val sourcePair=KeyPair.fromSecretSeed("SDDPXCR2SO7SUTV4JBQHLWQOP7DPDDRF7XL3GVPQKE6ZINHAIX4ZZFIH")
val issuerPair=KeyPair.fromSecretSeed("SBD2WR6L5XTRLBWCJJESXZ26RG4JL3SWKM4LASPJCJE4PSOHNDY3KHL4")
val distributionPair=KeyPair.fromSecretSeed("SC26JT6JWGTPO723TH5HZDUPUJQVWF32GKDEOZ5AFM6XQMPZQ4X5HJPG")
val bpAss = Asset.createNonNativeAsset("BrownyPoint", issuerPair.toPublicPair())
val tokenCap = Math.pow(10.0, 10.0)

class StellarWrapperTest {
    lateinit var server: Server
    lateinit var wrapper: KotlinStellarWrapper

    @BeforeEach
    fun before() {
        server = Server("http://localhost:8000")
        wrapper = KotlinStellarWrapper(server)
    }

    fun ensureBrownyPointCoinIssued() {
        // this stuff is slow so do it only once per test run
        // based on https://www.stellar.org/developers/guides/walkthroughs/custom-assets.html
        if (server.findAccount(sourcePair) == null) {
            logger.info("bootstrapping brownie point token")
            // we need enough tokens in the source account that we can create the other accounts
            wrapper.createAccount(amountLumen = 1000.0, newAccount = sourcePair)
            wrapper.createAccount(amountLumen = 100.0, sourceAccount = sourcePair, newAccount = issuerPair)
            wrapper.createAccount(amountLumen = 100.0, sourceAccount = sourcePair, newAccount = distributionPair)
            wrapper.trustAsset(distributionPair, bpAss, tokenCap)
            // issue the tokens
            wrapper.pay(bpAss, issuerPair, distributionPair,tokenCap)

            wrapper.setHomeDomain(issuerPair,"browniepoints.com")
            // prevent the issuer from ever issueing more tokens
            val proofTheIssuerCanIssueNoMore = wrapper.lockoutAccount(issuerPair)
            logger.info(proofTheIssuerCanIssueNoMore.resultXdr)
        } else {
            // this means accounts above already exist on your test chain
            // to start from scratch, you have to wipe that out
            logger.info { "brownie point already issued" }
        }
    }

    @Test
    fun `return null if account does not exist`() {
        val account = server.findAccount(KeyPair.random())
        account shouldBe null
    }

    @Test
    fun `use standalone root to create a new account`() {
        // this is the minimum on the standalone network; should be lower on the public stellars
        val amountLumen = 20.0
        val newKeyPair = wrapper.createAccount(amountLumen)

        try {
            val newAccount = server.accounts().account(newKeyPair)
            newAccount.balances[0].balance shouldBe "${amountLumen}000000"
        } catch (e: ErrorResponse) {
            logger.info("${e.code} ${e.body}")
            throw e
        }
    }

    @Test
    fun `distribute brownie points to a new account`() {
        ensureBrownyPointCoinIssued()

        // need larger opening balance for creating trust
        val anotherAccount = wrapper.createAccount(100.0, "receiver")

        wrapper.trustAsset(anotherAccount, bpAss, 100.0)
        wrapper.pay(bpAss, distributionPair, anotherAccount, 2.0)
        server.accounts().account(anotherAccount).balanceFor(bpAss)?.balanceAmount() shouldBe 2.0
    }

    @Test
    fun `trading with and without trust lines`() {
        ensureBrownyPointCoinIssued()

        val a1 = wrapper.createAccount(100.0, "receiver")
        wrapper.trustAsset(a1, bpAss, tokenCap)

        val a2 = wrapper.createAccount(100.0, "receiver")
        wrapper.trustAsset(a2, bpAss, tokenCap)


        // bp holders can pay each other without further need for trustlines
        wrapper.pay(bpAss, distributionPair,a1,100.0)
        wrapper.pay(bpAss, a1,a2,100.0)


        val a3NoTrust = wrapper.createAccount(100.0)

        shouldThrow<IllegalStateException> {
            wrapper.pay(bpAss, a2, a3NoTrust, 10.0)
        }.message should (contain("op_no_trust"))
    }

    @Test
    fun `concurrent transaction should retry`() {
        runBlocking {
            val tasks = mutableListOf<Deferred<KeyPair>>()
            for(i in 0..2) {
                tasks.add(async {
                    wrapper.createAccount(20.0,maxTries = 20)
                })
            }
            val all = awaitAll(*tasks.toTypedArray())
            all.size shouldBe 3
        }
    }
}