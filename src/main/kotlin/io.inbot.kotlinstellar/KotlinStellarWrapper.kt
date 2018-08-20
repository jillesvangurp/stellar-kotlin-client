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
import org.stellar.sdk.responses.SubmitTransactionResponse

private val logger = KotlinLogging.logger {}

/**
 * Helper that makes doing common operations against Stellar less boiler plate heavy.
 * @param server the Stellar Server instance
 * @param networkPassphrase if using a standalone chain, provide the password here. This enables you to use the root account for account creation.
 * @param minimumBalance minimumBalance for accounts. Currently 20.0 on standalone chaines but cheaper in the public network.
 */
class KotlinStellarWrapper(
    val server: Server,
    val networkPassphrase: String? = "Standalone Network ; February 2017",
    val minimumBalance: Double = 20.0,
    val maxRetries: Int = 10
) {
    /**
     * the keypair associated with the root account; only available if you have a passphrase
     */
    val rootKeyPair by lazy {
        if (networkPassphrase == null) {
            throw IllegalArgumentException("need to set networkPassphrase if you want root account access. This won't work on the testnet or public net for obvious reasons")
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
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst; default is 3
     */
    fun createNewAccount(amountLumen: Double, memo: String? = null, maxTries: Int = maxRetries): KeyPair {
        if (amountLumen < minimumBalance) {
            throw IllegalArgumentException("opening balance should be >= $minimumBalance XLM")
        }
        val newKeyPair = KeyPair.random()

        server.doTransaction(rootKeyPair, maxTries = maxTries) {
            addOperation(CreateAccountOperation.Builder(newKeyPair, amountLumen.toString()).build())
            if (memo != null) {
                addMemo(Memo.text(memo))
            }
        }
        return newKeyPair
    }

    /**
     * Create a trustline to an asset.
     * @param receiver account that trusts the asset
     * @param asset the asset
     * @param maxTrustedAmount maximum amount to which you trust the asset
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst; default is 3
     * @return transaction response
     */
    fun trustAsset(
        receiver: KeyPair,
        asset: Asset,
        maxTrustedAmount: Double,
        maxTries: Int = maxRetries
    ): SubmitTransactionResponse {
        return server.doTransaction(receiver, maxTries = maxTries) {
            addOperation(ChangeTrustOperation.Builder(asset, maxTrustedAmount.toString()).build())
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
        amount: Double,
        memo: String? = null,
        maxTries: Int = maxRetries
    ): SubmitTransactionResponse {
        return server.doTransaction(sender, maxTries = maxTries) {
            addOperation(PaymentOperation.Builder(receiver, asset, amount.toString()).build())
            if (memo != null) {
                addMemo(Memo.text(memo))
            }
        }
    }
}