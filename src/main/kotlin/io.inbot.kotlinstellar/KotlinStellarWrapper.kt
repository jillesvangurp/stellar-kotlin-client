package io.inbot.kotlinstellar

import mu.KotlinLogging
import org.stellar.sdk.Asset
import org.stellar.sdk.ChangeTrustOperation
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.CreatePassiveOfferOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.ManageOfferOperation
import org.stellar.sdk.Memo
import org.stellar.sdk.Network
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Server
import org.stellar.sdk.SetOptionsOperation
import org.stellar.sdk.assetCode
import org.stellar.sdk.doTransaction
import org.stellar.sdk.responses.OfferResponse
import org.stellar.sdk.responses.SubmitTransactionResponse

private val logger = KotlinLogging.logger {}

enum class StellarNetwork() {
    public, testnet, standalone
}

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
    val defaultMaxTries: Int = 10,
    val stellarNetwork: StellarNetwork = StellarNetwork.standalone
) {

    val network: Network?

    init {
        when (stellarNetwork) {
            StellarNetwork.standalone -> {
                network = Network(networkPassphrase)
                Network.use(network)
            }
            StellarNetwork.testnet -> {
                network = null
                Network.useTestNetwork()
            }
            StellarNetwork.public -> {
                network = null
                Network.usePublicNetwork()
            }
        }
    }

    /**
     * the keypair associated with the root account; only available if you have a passphrase
     */
    val rootKeyPair by lazy {
        if (networkPassphrase == null && stellarNetwork == StellarNetwork.standalone) {
            throw IllegalArgumentException("You need to set networkPassphrase if you want root account access. This won't work on the testnet or public net for obvious reasons")
        } else {
            if (network != null) {
                logger.info { "using standalone network" }
                KeyPair.fromSecretSeed(network.networkId)
            } else {
                throw IllegalStateException("cannot use root keypair when not on standalone network")
            }
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
    fun createAccount(
        amountLumen: TokenAmount,
        memo: String? = null,
        sourceAccount: KeyPair? = null,
        newAccount: KeyPair = KeyPair.random(),
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = if (sourceAccount != null) arrayOf(sourceAccount) else arrayOf(rootKeyPair)
    ): KeyPair {
        if (amountLumen < minimumBalance) {
            throw IllegalArgumentException("opening balance should be >= $minimumBalance XLM")
        }

        server.doTransaction(sourceAccount ?: rootKeyPair, maxTries = maxTries, signers = signers) {
            addOperation(CreateAccountOperation.Builder(newAccount, amountLumen.amount).build())
            if (memo != null) {
                addMemo(Memo.text(memo))
            }
        }
        logger.info { "created ${newAccount.accountId}" }
        return newAccount
    }

    /**
     * Create a trustline to an asset.
     * @param account account that trusts the asset
     * @param asset the asset
     * @param maxTrustedAmount maximum amount to which you trust the asset
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst; default is 3
     * @return transaction response
     */
    fun trustAsset(
        account: KeyPair,
        asset: Asset,
        maxTrustedAmount: TokenAmount,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account)
    ): SubmitTransactionResponse {
        return server.doTransaction(account, maxTries = maxTries, signers = signers) {
            addOperation(ChangeTrustOperation.Builder(asset, maxTrustedAmount.amount).build())
        }
    }

    fun setAccountOptions(
        account: KeyPair,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account),
        block: SetOptionsOperation.Builder.() -> Unit
    ): SubmitTransactionResponse {
        return server.doTransaction(account, maxTries = maxTries, signers = signers) {
            val setOptionsOperationBuilder = SetOptionsOperation.Builder()
            block.invoke(setOptionsOperationBuilder)
            addOperation(
                setOptionsOperationBuilder.build()
            )
        }
    }

    fun placeOffer(
        account: KeyPair,
        sell: TokenAmount,
        forAtLeast: TokenAmount,
        passive: Boolean = false,
        signers: Array<KeyPair> = arrayOf(account),
        maxTries: Int = defaultMaxTries
    ): SubmitTransactionResponse {
        val buyingAsset = forAtLeast.asset
        val sellingAsset = sell.asset
        if (buyingAsset != null && sellingAsset != null) {
            if (buyingAsset == sellingAsset) {
                throw IllegalArgumentException("buying and selling assets must be different")
            }
            val priceOf1SellingPerBuying = forAtLeast.divide(sell)
            return placeOffer(account, sellingAsset, buyingAsset, sell, priceOf1SellingPerBuying, passive, maxTries, signers = signers)
        } else {
            throw IllegalArgumentException("buying and selling amount must have non null assets")
        }
    }

    fun placeOffer(
        account: KeyPair,
        selling: Asset,
        buying: Asset,
        sellingAmount: TokenAmount,
        price: TokenAmount,
        passive: Boolean = false,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account)
    ): SubmitTransactionResponse {
        val response = server.doTransaction(account, maxTries, signers = signers) {
            logger.info { "place offer to sell ${sellingAmount.amount} ${selling.assetCode} for $price ${buying.assetCode}/${selling.assetCode} or ${price.inverse()} ${selling.assetCode}/${buying.assetCode}" }
            if (passive) {
                addOperation(
                    CreatePassiveOfferOperation.Builder(selling, buying, sellingAmount.amount, price.amount)
                        .build()
                )
            } else {
                addOperation(
                    ManageOfferOperation.Builder(selling, buying, sellingAmount.amount, price.amount)
                        .build()
                )
            }
        }
        return response
    }

    fun deleteOffer(
        account: KeyPair,
        offerResponse: OfferResponse,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account)
    ): SubmitTransactionResponse {
        return updateOffer(account, offerResponse, "0", offerResponse.price, maxTries = maxTries, signers = signers)
    }

    fun updateOffer(
        account: KeyPair,
        offerResponse: OfferResponse,
        sell: TokenAmount,
        forAtLeast: TokenAmount,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account)
    ): SubmitTransactionResponse {
        val buyingAsset = forAtLeast.asset
        val sellingAsset = sell.asset
        if (buyingAsset != null && sellingAsset != null) {
            if (buyingAsset == sellingAsset) {
                throw IllegalArgumentException("buying and selling assets must be different")
            }
            return updateOffer(account, offerResponse, sell.amount, forAtLeast.divide(sell).amount, maxTries, signers = signers)
        } else {
            throw IllegalArgumentException("buying and selling amount must have non null assets")
        }
    }

    fun updateOffer(
        account: KeyPair,
        offerResponse: OfferResponse,
        newAmountSelling: String,
        newPrice: String,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account)
    ): SubmitTransactionResponse {
        val response = server.doTransaction(account, maxTries, signers) {
            logger.info { "delete offer ${offerResponse.id}" }
            addOperation(
                ManageOfferOperation.Builder(offerResponse.selling, offerResponse.buying, newAmountSelling, newPrice)
                    .setOfferId(offerResponse.id)
                    .build()
            )
        }
        return response
    }

    fun deleteOffers(
        account: KeyPair,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account),
        limit: Int = 200
    ): SubmitTransactionResponse? {
        val records = server.offers().forAccount(account).limit(limit).execute().records
        if (records.size > 0) {
            return server.doTransaction(account, maxTries, signers = signers) {
                records.forEach {
                    addOperation(
                        ManageOfferOperation.Builder(it.selling, it.buying, "0", it.price)
                            .setOfferId(it.id)
                            .build()
                    )
                }
            }
        } else {
            return null
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
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(sender)
    ): SubmitTransactionResponse {
        return server.doTransaction(sender, maxTries = maxTries, signers = signers) {
            addOperation(PaymentOperation.Builder(receiver, asset, amount.amount).build())
            if (memo != null && memo.length > 0) {
                addMemo(Memo.text(memo))
            }
        }
    }
}