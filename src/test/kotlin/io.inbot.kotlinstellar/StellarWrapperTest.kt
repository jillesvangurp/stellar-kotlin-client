package io.inbot.ethclient.stellar

import com.google.common.math.LongMath
import io.inbot.kotlinstellar.KotlinStellarWrapper
import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.nativeXlmAsset
import io.inbot.kotlinstellar.tokenAmount
import io.inbot.kotlinstellar.xdrDecodeString
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
import org.junit.jupiter.api.assertThrows
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.Transaction
import org.stellar.sdk.amount
import org.stellar.sdk.findAccount
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.balanceAmount
import org.stellar.sdk.responses.balanceFor
import org.stellar.sdk.responses.describe
import org.stellar.sdk.responses.getTransactionResult
import org.stellar.sdk.responses.tokenAmount
import org.stellar.sdk.toPublicPair
import org.stellar.sdk.xdr.TransactionEnvelope
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger { }

// creating accounts is time consuming so try to reuse the same accounts and simply check if they exist before creating
val sourcePair = KeyPair.fromSecretSeed("SDDPXCR2SO7SUTV4JBQHLWQOP7DPDDRF7XL3GVPQKE6ZINHAIX4ZZFIH")
val issuerPair = KeyPair.fromSecretSeed("SBD2WR6L5XTRLBWCJJESXZ26RG4JL3SWKM4LASPJCJE4PSOHNDY3KHL4")
val distributionPair = KeyPair.fromSecretSeed("SC26JT6JWGTPO723TH5HZDUPUJQVWF32GKDEOZ5AFM6XQMPZQ4X5HJPG")
val bpt = Asset.createNonNativeAsset("bpt", issuerPair.toPublicPair())

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
            wrapper.pay(issuerPair, distributionPair, tokenCap, bpt)

            wrapper.setAccountOptions(issuerPair) {
                setHomeDomain("browniepoints.com")
            }
            // prevent the issuer from ever issueing more tokens
            val proofTheIssuerCanIssueNoMore = wrapper.setAccountOptions(issuerPair) {
                setMasterKeyWeight(0)
                setLowThreshold(0)
                setMediumThreshold(0)
                setHighThreshold(0)
            }

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
        wrapper.pay(distributionPair, anotherAccount, TokenAmount.of(2.0), bpt)
        server.accounts().account(anotherAccount).balanceFor(bpt)?.balanceAmount()?.amount shouldBe tokenAmount(2.0).amount
    }

    @Test
    fun `payments with and without trust lines`() {
        givenIssuedBroniePoints()

        val a1 = wrapper.createAccount(TokenAmount.of(100.0), "receiver")
        wrapper.trustAsset(a1, bpt, tokenCap)

        val a2 = wrapper.createAccount(TokenAmount.of(100.0), "receiver")
        wrapper.trustAsset(a2, bpt, tokenCap)

        // bp holders can pay each other without further need for trustlines
        wrapper.pay(distributionPair, a1, TokenAmount.of(100.0), bpt)
        wrapper.pay(a1, a2, TokenAmount.of(100.0), bpt)

        val a3NoTrust = wrapper.createAccount(TokenAmount.of(100.0))

        shouldThrow<IllegalStateException> {
            wrapper.pay(a2, a3NoTrust, TokenAmount.of(10.0), bpt)
        }.message should (contain("op_no_trust"))
    }

    @Test
    fun `buying some browny points using offers`() {
        givenIssuedBroniePoints()
        val theBuyer = wrapper.createAccount(TokenAmount.of(1000.0))
        wrapper.trustAsset(theBuyer, bpt, tokenCap)

        wrapper.placeOffer(distributionPair,
            tokenAmount(500.0, bpt),
            tokenAmount(1000.0, nativeXlmAsset)
        )
        wrapper.placeOffer(theBuyer,
            tokenAmount(10.0, nativeXlmAsset),
            tokenAmount(5.0, bpt)
        )

        val distAccount = server.accounts().account(distributionPair)
        val buyerAccount = server.accounts().account(theBuyer)
        val bptAmountAfterFirstOffer = buyerAccount.balanceFor(bpt)
        bptAmountAfterFirstOffer?.balanceAmount()?.amount shouldBe tokenAmount(5).amount
        println("distribution has $bptAmountAfterFirstOffer ${distAccount.balanceFor(nativeXlmAsset)}")
        println("buyer has ${buyerAccount.balanceFor(bpt)} ${buyerAccount.balanceFor(nativeXlmAsset)}")

        server.offers().forAccount(theBuyer).execute().records.size shouldBe 0

        // lets be unreasonable
        wrapper.placeOffer(theBuyer,
            tokenAmount(10, 0,nativeXlmAsset),
            tokenAmount(50,0, bpt)
        )
        server.offers().forAccount(theBuyer).execute().records.size shouldBe 1
        // the offer will not be fulfilled
        server.accounts().account(theBuyer).balanceFor(bpt)?.balanceAmount()?.amount shouldBe tokenAmount(5).amount
        // try again at a more reasonable rate
        wrapper.updateOffer(theBuyer, server.offers().forAccount(theBuyer).execute().records[0],
            tokenAmount(10, 0,nativeXlmAsset),
            tokenAmount(1, 0,bpt)
        )

        server.accounts().account(theBuyer).balanceFor(bpt)?.balanceAmount()?.amount shouldBe tokenAmount(10).amount // you get more than you bargained for

        // clean up
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

    @Test
    fun `multi sign a payment`() {
        val s1 = wrapper.createAccount(tokenAmount(200))
        val s2 = wrapper.createAccount(tokenAmount(200))
        val s3 = wrapper.createAccount(tokenAmount(200))
        val s4 = wrapper.createAccount(tokenAmount(200))

        val account = wrapper.createAccount(tokenAmount(200))

        printAccount(account)

        wrapper.pay(account, s1, tokenAmount(1.0), nativeXlmAsset)

        wrapper.setAccountOptions(account) {
            setSigner(s1.xdrSignerKey, 2)
        }

        wrapper.setAccountOptions(account) {
            setSigner(s2.xdrSignerKey, 2)
        }

        wrapper.setAccountOptions(account) {
            setSigner(s3.xdrSignerKey, 2)
        }

        wrapper.setAccountOptions(account) {
            setSigner(s4.xdrSignerKey, 2)
        }

        wrapper.setAccountOptions(account) {
            setHighThreshold(5)
            setMediumThreshold(3)
            setLowThreshold(1)
//            setMasterKeyWeight(0)
        }

        printAccount(account)

        wrapper.pay(account, s1, tokenAmount(1.0), nativeXlmAsset, signers = arrayOf(s2, s3))

        assertThrows<Exception> {
            // too many signatures is a problem: https://github.com/stellar/stellar-core/issues/1692
            wrapper.pay(account, s1, tokenAmount(1.0), nativeXlmAsset, signers = arrayOf(s2, s3, s4))
        }

        assertThrows<Exception> {
            wrapper.pay(account, s1, tokenAmount(1.0), nativeXlmAsset, signers = arrayOf(s1))
        }
        assertThrows<Exception> {
            wrapper.pay(account, s1, tokenAmount(1.0), nativeXlmAsset)
        }
    }

    fun printAccount(account: KeyPair) {
        val acc = server.accounts().account(account)
        println(
            """
                thresholds: ${acc.thresholds.lowThreshold} ${acc.thresholds.medThreshold} ${acc.thresholds.highThreshold}
                signers:
                ${acc.signers.map { s -> "\t${s.key} ${s.weight}" }.joinToString("\n")}
                authRequired: ${acc.flags.authRequired}
                authRevocable: ${acc.flags.authRevocable}
                """.trimIndent()
        )
    }

    @Test
    fun `pre-authorized payments`() {
        val alice = wrapper.createAccount(tokenAmount(100))
        val bob = wrapper.createAccount(tokenAmount(30))

        val (hash,xdr) = wrapper.preparePaymentTransaction(alice, bob, tokenAmount(10.0))

        val envelope = Transaction.fromEnvelopeXdr(xdrDecodeString(xdr, TransactionEnvelope::class))
        envelope.sign(hash.toByteArray(StandardCharsets.UTF_8))
        envelope.sign(alice)
        wrapper.server.submitTransaction(envelope)

        // pre authorized transaction; has not been payed yet; cashable by bob
        wrapper.server.accounts().account(bob).balanceFor(nativeXlmAsset)?.tokenAmount() shouldBe nativeXlmAsset.amount(30.0)

        wrapper.pay(alice,bob, nativeXlmAsset.amount(5))

        println(wrapper.server.accounts().account(alice).describe())

        val bobsTransaction = Transaction.fromEnvelopeXdr(xdrDecodeString(xdr, TransactionEnvelope::class))
        bobsTransaction.sign(bob)
        wrapper.server.submitTransaction(bobsTransaction)

        wrapper.server.accounts().account(bob).balanceFor(nativeXlmAsset)?.tokenAmount() shouldBe nativeXlmAsset.amount(40.0)
    }
}