package org.stellar.sdk

import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.tokenAmount
import java.nio.charset.StandardCharsets
import java.util.Base64
import mu.KotlinLogging
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.Validate
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.requests.TooManyRequestsException
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.SubmitTransactionResponse
import org.stellar.sdk.responses.SubmitTransactionTimeoutResponseException
import org.stellar.sdk.responses.describe

private val logger = KotlinLogging.logger {}

/**
 * Combine building and signing into a single call.
 */
fun Transaction.Builder.buildAndSign(vararg pairs: KeyPair): Transaction {
    pairs.forEach { it.validateCanSign() }
    val tx = build()
    pairs.forEach {
        tx.sign(it)
    }
    return tx
}

fun Server.findAccount(pair: KeyPair): AccountResponse? {
    return try {
        accounts().account(pair.accountId)
    } catch (e: ErrorResponse) {
        if (e.code == 404) {
            null
        } else {
            throw e
        }
    }
}

fun parseKeyPair(str: String?): KeyPair? {
    return try {
        KeyPair.fromSecretSeed(str)
    } catch (e: Exception) {
        try {
            KeyPair.fromAccountId(str)
        } catch (e: Exception) {
            null
        }
    }
}

fun KeyPair.seedString(): String {
    validateCanSign()
    return secretSeed.joinToString("")
}

fun KeyPair.toPublicPair(): KeyPair {
    return if (canSign()) {
        KeyPair.fromPublicKey(publicKey)
    } else {
        this
    }
}

fun KeyPair.validateCanSign() {
    if (!canSign()) {
        throw IllegalArgumentException("Pair has no private key")
    }
}

fun KeyPair.verify(data: String, base64Signature: String): Boolean {
    return verify(data.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(base64Signature))
}

fun KeyPair.sign(data: String): String {
    return Base64.getEncoder().encodeToString(sign(data.toByteArray(StandardCharsets.UTF_8)))
}

/**
 * asset code as a string (4 or 12 letter) or XLM if it is native.
 */
val Asset.assetCode: String
    get() {
        return if (this is AssetTypeCreditAlphaNum) {
            code
        } else {
            // native
            "XLM"
        }
    }

val Asset.assetIssuer: String?
    get() {
        return if (this.isNative()) null else (this as AssetTypeCreditAlphaNum).issuer
    }

fun Asset.isNative(): Boolean {
    return this is AssetTypeNative
}

fun Asset.describe(): String {
    return when (this) {
        is AssetTypeNative -> "XLM"
        else -> "$assetCode ($assetIssuer)"
    }
}

fun Asset.amount(value: Double): TokenAmount {
    return tokenAmount(value, this)
}

fun Asset.amount(value: String): TokenAmount {
    return tokenAmount(value, this)
}

fun Asset.amount(tokens: Long, stroops: Long = 0): TokenAmount {
    return tokenAmount(tokens, stroops, this)
}
fun Price.rate(): Double {
    return numerator.toDouble() / denominator.toDouble()
}

/**
 * Kotlin helper to create, sign, and submit a transaction.
 * @param forAccount account that signs the transaction
 * @param maxTries the number of times to retry in case of a tx_bad_seq response.
 * @param transactionBlock a block where you can add operations to the transaction
 * @return the response
 */
fun Server.doTransaction(
    network: Network,
    forAccount: KeyPair,
    maxTries: Int,
    signers: Array<KeyPair> = arrayOf(forAccount),
    transactionTimeout: Long = 15000, // 15 seconds
    baseFee: Int = 100, // this may require looking at fee stats
    sequenceNumberOverride: Long? = null, // we use this mainly in tests so we can trigger conflicts.
    transactionBlock: (Transaction.Builder).() -> Unit
): SubmitTransactionResponse {

    val response = doTransactionInternal(
        network,
        0,
        maxTries,
        forAccount,
        signers,
        transactionTimeout,
        baseFee,
        transactionBlock,
        sequenceNumberOverride
    )
    logger.info { response.describe() }
    return response
}

private fun Server.doTransactionInternal(
    network: Network,
    tries: Int,
    maxTries: Int,
    keyPair: KeyPair,
    signers: Array<KeyPair>,
    transactionTimeout: Long,
    baseFee: Int,
    transactionBlock: Transaction.Builder.() -> Unit,
    sequenceNumberOverride: Long? = null // we use this mainly in tests so we can trigger conflicts.

): SubmitTransactionResponse {
    keyPair.validateCanSign()

    val sourceAccount = if (sequenceNumberOverride == null) accounts().account(keyPair.accountId) else AccountResponse(keyPair.accountId, sequenceNumberOverride)

    // building a transaction actually increments this, yikes
    val currentSequenceNumber = sourceAccount.sequenceNumber

    Validate.isTrue(maxTries >= 0, "maxTries should be positive")
    val builder = Transaction.Builder(sourceAccount, network)
    builder.setTimeout(transactionTimeout)
    builder.setOperationFee(baseFee)

    transactionBlock.invoke(builder)
    val transaction = builder.buildAndSign(*signers)
    try {
        val response = submitTransaction(transaction)
        if (response.isSuccess) {
            if (tries > 0) {
                logger.info { "transaction succeeded after $tries tries. If you see this a lot, try not doing concurrent modifications against the same account" }
            }
            return response
        } else {
            val errorCode = response.extras.resultCodes?.transactionResultCode
            if (errorCode == "tx_bad_seq" && tries < maxTries) {
                // escalate how long it sleeps in between depending on the number of tries and randomize how long it sleeps
                // using increments of 1s because stellar transactions are relatively slow
                Thread.sleep(RandomUtils.nextLong(100, 1000 * (tries.toLong() + 1)))

                // retry and get latest sequence number
                return doTransactionInternal(
                    network,
                    tries + 1,
                    maxTries,
                    keyPair,
                    signers,
                    transactionTimeout,
                    baseFee,
                    transactionBlock
                )
            } else {
                val operationsFailures = response.extras.resultCodes?.operationsResultCodes?.joinToString(", ")
                throw IllegalStateException(
                    "failure after $tries transaction failed $errorCode - $operationsFailures ${response.describe()}"
                )
            }
        }
    } catch (e: SubmitTransactionTimeoutResponseException) {
        if (tries < maxTries) {
            Thread.sleep(1000)
            val latestAccount = this.accounts().account(sourceAccount.accountId)
            if (latestAccount.sequenceNumber == currentSequenceNumber) {
                // nothing happened on the blockchain? lets Try again
                logger.warn { "retrying $tries out of $maxTries because of a timeout" }
                return doTransactionInternal(
                    network,
                    tries + 1,
                    maxTries,
                    keyPair,
                    signers,
                    transactionTimeout,
                    baseFee,
                    transactionBlock,
                    sequenceNumberOverride = currentSequenceNumber
                )
            } else {

                // FIXME in case the sequence number went up, fetch the latest transaction and compare to what we would have sent to verify if the transaction happened as planned
                logger.warn { "conflicting transaction on account sequence number changed from ${sourceAccount.sequenceNumber} to ${latestAccount.sequenceNumber}" }
                throw java.lang.IllegalStateException("Timeout and the account sequence number increased from ${sourceAccount.sequenceNumber} to ${latestAccount.sequenceNumber} on on ${keyPair.accountId}; skipping retry to avoid duplicate transaction")
            }
        } else {
            logger.error { "failing after too many tries: ${e::class.qualifiedName} ${e.message}" }
            throw e
        }
    } catch (e: TooManyRequestsException) {
        if (tries < maxTries) {
            Thread.sleep(2000)
            logger.warn { "retrying $tries out of $maxTries because of a timeout" }
            return doTransactionInternal(
                network,
                tries + 1,
                maxTries,
                keyPair,
                signers,
                transactionTimeout,
                baseFee,
                transactionBlock,
                sequenceNumberOverride = currentSequenceNumber
            )
        } else {
            logger.error { "failing after too many tries: ${e::class.qualifiedName} ${e.message}" }
            throw e
        }
    } catch (e: ErrorResponse) {
        // FIXME check for mismatching ledger
        // FIXME check for base fee issues and implement mechanism to retry with higher fee
        logger.error { "ERROR ${e.code} ${e.body}" }
        throw e
    }
}
