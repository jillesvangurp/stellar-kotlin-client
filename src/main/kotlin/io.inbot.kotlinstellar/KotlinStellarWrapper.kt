package io.inbot.kotlinstellar

import mu.KotlinLogging
import org.stellar.sdk.Asset
import org.stellar.sdk.ChangeTrustOperation
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Memo
import org.stellar.sdk.Network
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Server
import org.stellar.sdk.SetOptionsOperation
import org.stellar.sdk.responses.SubmitTransactionResponse

private val logger = KotlinLogging.logger {}

/**
 * Helper that makes doing common operations against Stellar less boiler plate heavy.
 * @param server the Stellar Server instance
 * @param networkPassphrase if using a standalone chain, provide the password here. This enables you to use the root account for account creation.
 * @param minimumBalance minimumBalance for accounts. Currently 20.0 on standalone chaines but cheaper in the public network.
 * @param defaultMaxTries default amount of times a transaction is retried in case of conflicts with the sequence number (tx_bad_seq) before failing; default is 10
 */
class KotlinStellarWrapper(
    val server: Server,
    val networkPassphrase: String? = "Standalone Network ; February 2017",
    val minimumBalance: TokenAmount = TokenAmount.of(20, 0),
    val defaultMaxTries: Int = 10
) {
    /**
     * the keypair associated with the root account; only available if you have a passphrase
     */
    val rootKeyPair by lazy {
        if (networkPassphrase == null) {
            throw IllegalArgumentException("You need to set networkPassphrase if you want root account access. This won't work on the testnet or public net for obvious reasons")
        } else {
            val standAloneNw = Network("Standalone Network ; February 2017")
            Network.use(standAloneNw)
            val networkId = standAloneNw.networkId
            KeyPair.fromSecretSeed(networkId)
        }
    }

    /**
     * Create a new account and return the keyPair.
     * @param opening balance. Needs to be >= the minimum balance for your network.
     * @param memo optional string memo
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst
     * @param sourceAccount account that creates the newAccount. If null, the rootAccount will be used.
     * @param newAccount account that will be created; defaults to a random key pair
     * @return the key pair of the created account
     */
    fun createAccount(amountLumen: TokenAmount, memo: String? = null, sourceAccount: KeyPair? = null, newAccount: KeyPair = KeyPair.random(), maxTries: Int = defaultMaxTries): KeyPair {
        if (amountLumen < minimumBalance) {
            throw IllegalArgumentException("opening balance should be >= $minimumBalance XLM")
        }

        server.doTransaction(sourceAccount ?: rootKeyPair, maxTries = maxTries) {
            addOperation(CreateAccountOperation.Builder(newAccount, amountLumen.toString()).build())
            if (memo != null) {
                addMemo(Memo.text(memo))
            }
        }
        return newAccount
    }

    /**
     * Create a trustline to an asset.
     * @param signer account that trusts the asset
     * @param asset the asset
     * @param maxTrustedAmount maximum amount to which you trust the asset
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst; default is 3
     * @return transaction response
     */
    fun trustAsset(
        signer: KeyPair,
        asset: Asset,
        maxTrustedAmount: TokenAmount,
        maxTries: Int = defaultMaxTries
    ): SubmitTransactionResponse {
        return server.doTransaction(signer, maxTries = maxTries) {
            addOperation(ChangeTrustOperation.Builder(asset, maxTrustedAmount.toString()).build())
        }
    }

    fun setHomeDomain(keyPair: KeyPair, domain: String, maxTries: Int = defaultMaxTries): SubmitTransactionResponse {
        return server.doTransaction(keyPair, maxTries = maxTries) {
            addOperation(
                SetOptionsOperation.Builder()
                    .setHomeDomain(domain)
                    .build()
            )
        }
    }

    fun lockoutAccount(keyPair: KeyPair, maxTries: Int = defaultMaxTries): SubmitTransactionResponse {
        return server.doTransaction(keyPair, maxTries = maxTries) {
            addOperation(
                SetOptionsOperation.Builder()
                    .setMasterKeyWeight(0)
                    .setLowThreshold(0)
                    .setMediumThreshold(0)
                    .setHighThreshold(0)
                    .build()
            )
        }
    }

    /**
     * Create a trustline to an asset.
     * @param asset the asset
     * @param sender sender account
     * @param receiver receiver account
     * @param amount amount to be sent
     * @param memo optinoal text memo
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst; default is 3
     * @return transaction response
     */
    fun pay(
        asset: Asset,
        sender: KeyPair,
        receiver: KeyPair,
        amount: TokenAmount,
        memo: String? = null,
        maxTries: Int = defaultMaxTries
    ): SubmitTransactionResponse {
        return server.doTransaction(sender, maxTries = maxTries) {
            addOperation(PaymentOperation.Builder(receiver, asset, amount.toString()).build())
            if (memo != null) {
                addMemo(Memo.text(memo))
            }
        }
    }
}