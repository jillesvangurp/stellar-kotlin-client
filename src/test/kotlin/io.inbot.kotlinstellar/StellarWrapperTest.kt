package io.inbot.ethclient.stellar

import com.google.common.math.LongMath
import io.inbot.kotlinstellar.KotlinStellarWrapper
import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.amount
import io.inbot.kotlinstellar.balanceAmount
import io.inbot.kotlinstellar.balanceFor
import io.inbot.kotlinstellar.findAccount
import io.inbot.kotlinstellar.getTransactionResult
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
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse

private val logger = KotlinLogging.logger { }

// creating accounts is time consuming so try to reuse the same accounts and simply check if they exist before creating
val sourcePair = KeyPair.fromSecretSeed("SDDPXCR2SO7SUTV4JBQHLWQOP7DPDDRF7XL3GVPQKE6ZINHAIX4ZZFIH")
val issuerPair = KeyPair.fromSecretSeed("SBD2WR6L5XTRLBWCJJESXZ26RG4JL3SWKM4LASPJCJE4PSOHNDY3KHL4")
val distributionPair = KeyPair.fromSecretSeed("SC26JT6JWGTPO723TH5HZDUPUJQVWF32GKDEOZ5AFM6XQMPZQ4X5HJPG")
val bpt = Asset.createNonNativeAsset("bpt", issuerPair.toPublicPair())
val native = AssetTypeNative()

val tokenCap = TokenAmount.of(LongMath.pow(10, 10), 0)

class StellarWrapperTest {
    lateinit var server: Server
    lateinit var wrapper: KotlinStellarWrapper

    @BeforeEach
    fun before() {
        server = Server("http://localhost:8000")
        wrapper = KotlinStellarWrapper(server)
    }

    fun givenIssuedBroniePoints() {
        // this stuff is slow so do it only once per test run
        // based on https://www.stellar.org/developers/guides/walkthroughs/custom-assets.html
        if (server.findAccount(sourcePair) == null) {
            logger.info("bootstrapping brownie point token")
            // we need enough tokens in the source account that we can create the other accounts
            wrapper.createAccount(amountLumen = TokenAmount.of(100000, 0), newAccount = sourcePair)
            // use the minimum amount because we'll lock this account down after issueing
            // + 1 because the transfer will drop us below the minimum amount
            // TODO figure out the absolute minimums in stroops here
            wrapper.createAccount(amountLumen = TokenAmount.of(1000, 0), sourceAccount = sourcePair, newAccount = issuerPair)
            wrapper.createAccount(amountLumen = TokenAmount.of(1000, 0), sourceAccount = sourcePair, newAccount = distributionPair)
            wrapper.trustAsset(distributionPair, bpt, tokenCap)
            // issue the tokens
            wrapper.pay(bpt, issuerPair, distributionPair, tokenCap)

            wrapper.setHomeDomain(issuerPair, "browniepoints.com")
            // prevent the issuer from ever issueing more tokens
            val proofTheIssuerCanIssueNoMore = wrapper.lockoutAccount(issuerPair)

            proofTheIssuerCanIssueNoMore.getTransactionResult().result.results.forEach {
                println("${it.tr.discriminant.name} ${it.tr.setOptionsResult.discriminant.name} ")
            }
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
        val amountLumen = TokenAmount.of(20, 0)
        val newKeyPair = wrapper.createAccount(amountLumen)

        try {
            val newAccount = server.accounts().account(newKeyPair)
            newAccount.balances[0].balanceAmount() shouldBe amountLumen
        } catch (e: ErrorResponse) {
            logger.info("${e.code} ${e.body}")
            throw e
        }
    }

    @Test
    fun `distribute brownie points to a new account`() {
        givenIssuedBroniePoints()

        // need larger opening balance for creating trust
        val anotherAccount = wrapper.createAccount(TokenAmount.of(100.0), "receiver")

        wrapper.trustAsset(anotherAccount, bpt, TokenAmount.of(100.0))
        wrapper.pay(bpt, distributionPair, anotherAccount, TokenAmount.of(2.0))
        server.accounts().account(anotherAccount).balanceFor(bpt).amount shouldBe amount(2.0).amount
    }

    @Test
    fun `payments with and without trust lines`() {
        givenIssuedBroniePoints()

        val a1 = wrapper.createAccount(TokenAmount.of(100.0), "receiver")
        wrapper.trustAsset(a1, bpt, tokenCap)

        val a2 = wrapper.createAccount(TokenAmount.of(100.0), "receiver")
        wrapper.trustAsset(a2, bpt, tokenCap)

        // bp holders can pay each other without further need for trustlines
        wrapper.pay(bpt, distributionPair, a1, TokenAmount.of(100.0))
        wrapper.pay(bpt, a1, a2, TokenAmount.of(100.0))

        val a3NoTrust = wrapper.createAccount(TokenAmount.of(100.0))

        shouldThrow<IllegalStateException> {
            wrapper.pay(bpt, a2, a3NoTrust, TokenAmount.of(10.0))
        }.message should (contain("op_no_trust"))
    }

    @Test
    fun `buying some browny points using xlm`() {
        givenIssuedBroniePoints()
        val theBuyer = wrapper.createAccount(TokenAmount.of(1000.0))
        wrapper.trustAsset(theBuyer, bpt, tokenCap)

        wrapper.placeOffer(distributionPair, amount(500, bpt), amount(1000, native))
        wrapper.placeOffer(theBuyer, amount(10, native), amount(5, bpt))

        val distAccount = server.accounts().account(distributionPair)
        val buyerAccount = server.accounts().account(theBuyer)
        println("distribution has ${distAccount.balanceFor(bpt)} ${distAccount.balanceFor(native)}")
        println("buyer has ${buyerAccount.balanceFor(bpt)} ${buyerAccount.balanceFor(native)}")

        server.offers().forAccount(theBuyer).execute().records.size shouldBe 0

        wrapper.deleteOffers(theBuyer)
        wrapper.deleteOffers(distributionPair)
    }

    @Test
    fun `concurrent transaction should retry`() {
        runBlocking {
            val tasks = mutableListOf<Deferred<KeyPair>>()
            for (i in 0..2) {
                tasks.add(async {
                    wrapper.createAccount(TokenAmount.of(20.0), maxTries = 20)
                })
            }
            val all = awaitAll(*tasks.toTypedArray())
            all.size shouldBe 3
        }
    }
}