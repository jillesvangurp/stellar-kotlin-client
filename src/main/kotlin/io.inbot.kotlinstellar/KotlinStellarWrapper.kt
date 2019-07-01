package io.inbot.kotlinstellar

import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.stellar.sdk.Asset
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.ChangeTrustOperation
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.CreatePassiveSellOfferOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.ManageSellOfferOperation
import org.stellar.sdk.Memo
import org.stellar.sdk.Network
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Price
import org.stellar.sdk.Server
import org.stellar.sdk.SetOptionsOperation
import org.stellar.sdk.Transaction
import org.stellar.sdk.assetCode
import org.stellar.sdk.assetIssuer
import org.stellar.sdk.describe
import org.stellar.sdk.doTransaction
import org.stellar.sdk.findAccount
import org.stellar.sdk.isNative
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.requests.TooManyRequestsException
import org.stellar.sdk.responses.AssetResponse
import org.stellar.sdk.responses.OfferResponse
import org.stellar.sdk.responses.Page
import org.stellar.sdk.responses.Response
import org.stellar.sdk.responses.SubmitTransactionResponse
import org.stellar.sdk.responses.TradeAggregationResponse
import org.stellar.sdk.responses.TradeResponse
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.responses.balanceFor
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import org.stellar.sdk.responses.tokenAmount
import org.stellar.sdk.xdr.TransactionEnvelope
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import kotlin.reflect.full.cast

private val logger = KotlinLogging.logger {}

val nativeXlmAsset = AssetTypeNative()

/**
 * Helper that makes doing common operations against Stellar less boiler plate heavy.
 * @param server the Stellar Server instance
 * @param networkPassphrase if using a standalone chain, provide the password here. This enables you to use the root account for account creation.
 * @param minimumBalance minimumBalance for accounts. Currently 20.0 on standalone chaines but cheaper in the public network.
 * @param defaultMaxTries default amount of times a transaction is retried in case of conflicts with the sequence number (tx_bad_seq) before failing; default is 10
 */
class KotlinStellarWrapper(
    val server: Server,
    val minimumBalance: TokenAmount = TokenAmount.of(1, 0),
    val defaultMaxTries: Int = 10,
    val network: Network = Network("Standalone Network ; February 2017")
) {
    /**
     * the keypair associated with the root account; should only be used on standalone networks.
     */
    val rootKeyPair by lazy {
        if (network.networkPassphrase == Network.PUBLIC.networkPassphrase || network.networkPassphrase == Network.TESTNET.networkPassphrase) {
            null
        } else {
            KeyPair.fromSecretSeed(network.networkId)
        }
    }

    fun testConnection(): Boolean {
        val root = server.root()
        return root.coreLatestLedger > 0 && root.historyLatestLedger == root.coreLatestLedger
    }

    fun awaitServerIsRunning() {
        while (!testConnection()) {
            logger.info { "waiting for server to be alive" }
            Thread.sleep(1000)
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
        signers: Array<KeyPair> = if (sourceAccount != null) arrayOf(sourceAccount) else arrayOf(rootKeyPair ?: throw IllegalStateException("signers are required if not running on a standalone net"))
    ): KeyPair {
        if (amountLumen < minimumBalance) {
            throw IllegalArgumentException("opening balance should be >= $minimumBalance XLM")
        }

        val account = when {
            sourceAccount != null -> sourceAccount
            rootKeyPair != null -> rootKeyPair!!
            else -> throw IllegalStateException("source account is required when not running on a standAlone network")
        }
        server.doTransaction(network, account, maxTries = maxTries, signers = signers) {
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
        return server.doTransaction(network, account, maxTries = maxTries, signers = signers) {
            addOperation(ChangeTrustOperation.Builder(asset, maxTrustedAmount.amount).build())
        }
    }

    fun setAccountOptions(
        account: KeyPair,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(account),
        block: SetOptionsOperation.Builder.() -> Unit
    ): SubmitTransactionResponse {
        return server.doTransaction(network, account, maxTries = maxTries, signers = signers) {
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
            return placeOffer(
                account,
                sellingAsset,
                buyingAsset,
                sell,
                forAtLeast / sell,
                passive,
                maxTries,
                signers = signers
            )
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
        val response = server.doTransaction(network, account, maxTries, signers = signers) {
            logger.info { "place offer to sell ${sellingAmount.amount} ${selling.assetCode} for $price ${buying.assetCode}/${selling.assetCode} or ${price.inverse()} ${selling.assetCode}/${buying.assetCode}" }
            if (passive) {
                addOperation(
                    CreatePassiveSellOfferOperation.Builder(selling, buying, sellingAmount.amount, price.amount)
                        .build()
                )
            } else {
                addOperation(
                    ManageSellOfferOperation.Builder(selling, buying, sellingAmount.amount, price.amount)
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
            return updateOffer(
                account,
                offerResponse,
                sell.amount,
                (forAtLeast / sell).amount,
                maxTries,
                signers = signers
            )
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
        val response = server.doTransaction(network, account, maxTries, signers) {
            logger.info { "delete offer ${offerResponse.id}" }
            addOperation(
                ManageSellOfferOperation.Builder(
                    offerResponse.selling,
                    offerResponse.buying,
                    newAmountSelling,
                    newPrice
                )
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
            return server.doTransaction(network, account, maxTries, signers = signers) {
                records.forEach {
                    addOperation(
                        ManageSellOfferOperation.Builder(it.selling, it.buying, "0", it.price)
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
     * @param sender sender account
     * @param receiver receiver account
     * @param amount amount to be sent
     * @param asset the asset
     * @param memo optinoal text memo
     * @param maxTries maximum amount of times to retry the transaction in case of conflicst; default is 3
     * @return transaction response
     */
    fun pay(
        sender: KeyPair,
        receiver: KeyPair,
        amount: TokenAmount,
        asset: Asset = amount.asset ?: nativeXlmAsset,
        memo: String? = null,
        maxTries: Int = defaultMaxTries,
        signers: Array<KeyPair> = arrayOf(sender),
        validate: Boolean = true
    ): SubmitTransactionResponse {
        if (validate) {
            val validationResponse =
                isPaymentPossible(sender, receiver, TokenAmount.ofStroops(amount.totalStroops, asset))
            if (!validationResponse.first) {
                throw IllegalArgumentException("validation failure: ${validationResponse.second}")
            }
        }
        return server.doTransaction(network, sender, maxTries = maxTries, signers = signers) {
            addOperation(PaymentOperation.Builder(receiver, asset, amount.amount).build())
            if (StringUtils.isNotBlank(memo)) {
                if (memo!!.toByteArray(StandardCharsets.UTF_8).size > 28) {
                    throw IllegalStateException("Memo exceeds limit of 28 bytes")
                }
                addMemo(Memo.text(memo))
            }
        }
    }

    data class PreparedTransaction(val transactionHash: String, val transactionEnvelopeXdr: String)

    fun preparePaymentTransaction(
        sender: KeyPair,
        receiver: KeyPair,
        amount: TokenAmount,
        asset: Asset = amount.asset ?: nativeXlmAsset,
        memo: String? = null,
        validate: Boolean = true,
        transactionTimeout: Long = Transaction.Builder.TIMEOUT_INFINITE,
        baseFee: Int = 100
    ): PreparedTransaction {
        if (validate) {
            val validationResponse =
                isPaymentPossible(sender, receiver, TokenAmount.ofStroops(amount.totalStroops, asset))
            if (!validationResponse.first) {
                throw IllegalArgumentException("validation failure: ${validationResponse.second}")
            }
        }

        val txBuilder = Transaction.Builder(server.accounts().account(sender), network)
            .setOperationFee(baseFee)
            .addOperation(PaymentOperation.Builder(receiver, asset, amount.toString()).build())
        if (StringUtils.isNotBlank(memo)) {
            if (memo!!.toByteArray(StandardCharsets.UTF_8).size > 28) {
                throw IllegalStateException("Memo exceeds limit of 28 bytes")
            }
            txBuilder.addMemo(Memo.text(memo))
        }
        val tx = txBuilder.setTimeout(transactionTimeout).build()
        val transactionEnvelope = TransactionEnvelope()
        transactionEnvelope.tx = tx.toXdr()
        transactionEnvelope.signatures = arrayOf()

        return PreparedTransaction(
            Base64.getEncoder().encode(tx.hash()).toString(StandardCharsets.UTF_8),
            xdrEncode(transactionEnvelope)
        )
    }

    private fun <T : Response> pageSequence(
        cursorExtractorFunction: (T) -> String,
        nextPageFunction: (String) -> Page<T>,
        cursor: String,
        endless: Boolean,
        pollingIntervalMs: Long,
        sleepOnThrottle: Long // paging is highly likely to exceed rate limiting
    ): Sequence<Page<T>> {
        var currentCursor = cursor
        var catchingUp = true
        return generateSequence {
            Thread.sleep(pollingIntervalMs)
            var page: Page<T>
            page = try {
                nextPageFunction.invoke(currentCursor)
            } catch (e: TooManyRequestsException) {
                Thread.sleep(sleepOnThrottle) // slow down
                nextPageFunction.invoke(currentCursor)
            }
            // make sure we don't return an empty page because that will kill the sequence
            while (page.records.size == 0 && endless) {
                Thread.sleep(pollingIntervalMs)

                page = try {
                    nextPageFunction.invoke(currentCursor)
                } catch (e: TooManyRequestsException) {
                    Thread.sleep(sleepOnThrottle) // slow down
                    nextPageFunction.invoke(currentCursor)
                }
            }

            // we need this because pagingToken is not part of the Response API for some reason
            if (catchingUp && page.records.size == 0) {
                catchingUp = false
            }
            if (page.records.size == 0) {
                null
            } else {
                currentCursor = cursorExtractorFunction.invoke(page.records.last())
                page
            }
        }
    }

    fun offersSequence(
        account: KeyPair? = null,
        cursor: String = "now",
        fetchSize: Int = 10,
        endless: Boolean = false,
        pollingIntervalMs: Long = 5000,
        sleepOnThrottle: Long

    ): Sequence<OfferResponse> {
        val fetch = { c: String ->
            val builder = server.offers()
            if (account != null) builder.forAccount(account)
            builder.cursor(c).limit(fetchSize).execute()
        }

        return pageSequence({ it -> it.pagingToken }, fetch, cursor, endless, pollingIntervalMs, sleepOnThrottle)
            .flatMap { it.records.asSequence() }
    }

    fun operationsSequence(
        account: KeyPair? = null,
        ledger: Long? = null,
        cursor: String = "now",
        fetchSize: Int = 10,
        endless: Boolean = false,
        pollingIntervalMs: Long = 5000,
        sleepOnThrottle: Long

    ): Sequence<OperationResponse> {
        val fetch = { c: String ->
            val builder = server.operations()
            if (account != null) builder.forAccount(account)
            if (ledger != null) builder.forLedger(ledger)

            builder.cursor(c).limit(fetchSize).execute()
        }

        return pageSequence({ it -> it.pagingToken }, fetch, cursor, endless, pollingIntervalMs, sleepOnThrottle)
            .flatMap { it.records.asSequence() }
    }

    fun transactionsSequence(
        account: KeyPair? = null,
        ledger: Long? = null,
        cursor: String = "now",
        fetchSize: Int = 10,
        endless: Boolean = false,
        pollingIntervalMs: Long = 5000,
        sleepOnThrottle: Long

    ): Sequence<TransactionResponse> {
        val fetch = { c: String ->
            val builder = server.transactions()
            if (account != null) builder.forAccount(account)
            if (ledger != null) builder.forLedger(ledger)
            builder.cursor(c).limit(fetchSize).execute()
        }

        return pageSequence({ it -> it.pagingToken }, fetch, cursor, endless, pollingIntervalMs, sleepOnThrottle)
            .flatMap { it.records.asSequence() }
    }

    fun assetsSequence(
        assetIssuer: String? = null,
        assetCode: String? = null,
        cursor: String = "now",
        fetchSize: Int = 200,
        endless: Boolean = false,
        pollingIntervalMs: Long = 5000,
        sleepOnThrottle: Long

    ): Sequence<AssetResponse> {
        val fetch = { c: String ->
            val builder = server.assets()

            if (assetIssuer != null) builder.assetIssuer(assetIssuer)
            if (assetCode != null) builder.assetCode(assetCode)
            builder.cursor(c).limit(fetchSize)

            builder.execute()
        }

        return pageSequence({ it -> it.pagingToken }, fetch, cursor, endless, pollingIntervalMs, sleepOnThrottle)
            .flatMap { it.records.asSequence() }
    }

    fun paymentSequence(
        account: KeyPair? = null,
        ledger: Long? = null,
        transactionId: String? = null,
        cursor: String = "now",
        fetchSize: Int = 10,
        endless: Boolean = false,
        pollingIntervalMs: Long = 5000
    ): Sequence<PaymentOperationResponse> {

        val fetchPageFunction = { nextCursor: String ->
            val builder = server.payments()
            if (account != null) builder.forAccount(account)
            if (ledger != null) builder.forLedger(ledger)
            if (transactionId != null) builder.forTransaction(transactionId)
            val requestBuilder = builder.limit(fetchSize).cursor(nextCursor)
            val page = requestBuilder.execute()
            page
        }
        val pageSequence = pageSequence<OperationResponse>(
            { it -> it.pagingToken }, fetchPageFunction,
            cursor, endless, pollingIntervalMs, 5000
        )

        return pageSequence.flatMap { it.records.asSequence() }
            // filter out anything that isn't a payment (account creation is included for some reason)
            .filter { it is PaymentOperationResponse }
            .map { PaymentOperationResponse::class.cast(it) }
    }

    fun isPaymentPossible(sender: KeyPair, receiver: KeyPair, tokenAmount: TokenAmount): Pair<Boolean, String> {
        if (tokenAmount.asset == null) {
            return false to "No asset specified on tokenAmount $tokenAmount"
        }
        val senderAccount = server.findAccount(sender)
            ?: return false to "Sender ${sender.accountId} does not exist"

        if (sender.accountId != tokenAmount.asset.assetIssuer || tokenAmount.asset.isNative()) {
            // issuer should be able to issue and won't have a balance. The exception is native XLM.
            val currentSenderBalance = senderAccount.balanceFor(tokenAmount.asset)
                ?: return false to "Sender ${sender.accountId} does not have any ${tokenAmount.asset.describe()}"
            if (currentSenderBalance.tokenAmount() < tokenAmount) {
                return false to "Sender ${sender.accountId} does not have enough ${tokenAmount.asset.describe()} to transfer $tokenAmount. Current balance: ${currentSenderBalance.tokenAmount()}"
            }
        }

        val receiverAccount = server.findAccount(receiver)
            ?: return false to "Receiver ${receiver.accountId} does not exist"
        if (!tokenAmount.asset.isNative()) {
            if (tokenAmount.asset.assetIssuer != receiver.accountId) {
                // skip trust line check if we are destroying tokens by sending them to the issuer
                val currentReceiverBalance = receiverAccount.balanceFor(tokenAmount.asset)
                    ?: return false to "Receiver ${receiver.accountId} does not have a trust line for ${tokenAmount.asset.describe()}"
                val trustLineLimit = tokenAmount(currentReceiverBalance.limit)
                if (trustLineLimit < tokenAmount) {
                    return false to "Receiver ${sender.accountId} trust line limit of $trustLineLimit is not enough to receive $tokenAmount."
                }
            }
        }

        return true to "OK"
    }

    fun mostRecentPrice(baseAsset: Asset, counterAsset: Asset): Price? {
        return try {
            trades(baseAsset, counterAsset, limit = 1, descending = true).first().price
        } catch (e: NoSuchElementException) {
            null
        }
    }

    fun trades(
        baseAsset: Asset? = null,
        counterAsset: Asset? = null,
        offerId: String? = null,
        account: KeyPair? = null,
        cursor: String? = null,
        limit: Int = 20,
        descending: Boolean = false
    ): Sequence<TradeResponse> {
        val builder = server.trades()
        if (baseAsset != null) {
            builder.baseAsset(baseAsset)
        }
        if (counterAsset != null) {
            builder.counterAsset(counterAsset)
        }
        if (offerId != null) {
            builder.offerId(offerId)
        }
        if (account != null) {
            builder.forAccount(account)
        }
        if (cursor != null) {
            builder.cursor(cursor)
        }

        builder.order(if (descending) RequestBuilder.Order.DESC else RequestBuilder.Order.ASC)
        builder.limit(limit)
        return builder.execute().records.iterator().asSequence()
    }

    fun tradeAggs(
        baseAsset: Asset,
        counterAsset: Asset = nativeXlmAsset,
        from: Instant,
        to: Instant,
        resolution: TradeAggregationResolution,
        cursor: String? = null,
        limit: Int = 20,
        descending: Boolean = false,
        offSet: Long = 0L
    ): Sequence<TradeAggregationResponse> {
        val builder = server.tradeAggregations(
            baseAsset, counterAsset, from.toEpochMilli(), to.toEpochMilli(), resolution.resolution,
            offSet
        )
        if (cursor != null) {
            builder.cursor(cursor)
        }
        builder.order(if (descending) RequestBuilder.Order.DESC else RequestBuilder.Order.ASC)
        builder.limit(limit)
        return builder.execute().records.iterator().asSequence()
    }
}