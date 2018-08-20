package io.inbot.kotlinstellar

import mu.KotlinLogging
import org.apache.commons.lang3.RandomUtils
import org.stellar.sdk.Asset
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.Transaction
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.SubmitTransactionResponse

// some extensions for classes in the Stellar SDK
private val logger = KotlinLogging.logger {}

/**
 * Return a human readable multi line report with relevant details about the account.
 */
fun AccountResponse.describe(): String {
    return """
accountId: ${keypair.accountId}
subEntryCount: $subentryCount
home domain: $homeDomain

Signers
${signers.map { "${it.key} ${it.weight}" }.joinToString("\n")}
Balances:
${balances.map { "balance ${it.assetCode} ${it.assetType} ${it.balance} ${it.limit} ${it.buyingLiabilities} ${it.sellingLiabilities}" }.joinToString(
"\n"
)}
Links:
${links.effects.href}
${links.offers.href}
${links.operations.href}
${links.transactions.href}
${links.self.href}
""".trimIndent()
}

/**
 * Return the balances as a map for easy lookups.
 */
fun AccountResponse.balanceMap(): Map<Asset, AccountResponse.Balance> {
    val map = mutableMapOf<Asset, AccountResponse.Balance>()
    balances.forEach {
        map.put(it.asset, it)
    }
    return map.toMap()
}

/**
 * Return the balance for a specific Asset.
 */
fun AccountResponse.balanceFor(asset: Asset): AccountResponse.Balance? {
    return balanceMap()[asset]
}

/**
 * Combine building and signing into a single call.
 */
fun Transaction.Builder.buildAndSign(pair: KeyPair): Transaction {
    val tx = build()
    tx.sign(pair)
    return tx
}

/**
 * Kotlin helper to create, sign, and submit a transaction.
 * @param keyPair account that signs the transaction
 * @param maxTries the number of times to retry in case of a tx_bad_seq response.
 * @param transactionBlock a block where you can add operations to the transaction
 * @return the response
 */
fun Server.doTransaction(
    keyPair: KeyPair,
    maxTries: Int,
    transactionBlock: (Transaction.Builder).() -> Unit
): SubmitTransactionResponse {
    try {
        return doTransactionInternal(0, maxTries, keyPair, transactionBlock)
    } catch (e: ErrorResponse) {
        logger.warn("${e.code} - ${e.body}")

        throw e
    }
}

private fun Server.doTransactionInternal(
    tries: Int,
    maxTries: Int,
    keyPair: KeyPair,
    transactionBlock: (Transaction.Builder).() -> Unit
): SubmitTransactionResponse {
    val builder = Transaction.Builder(accounts().account(keyPair))
    transactionBlock.invoke(builder)
    val transaction = builder.buildAndSign(keyPair)
    val response = submitTransaction(transaction)
    if (response.isSuccess) {
        if (tries>0) {
            logger.info { "transaction succeeded after $tries tries. If you see this a lot, try not doing concurrent modifications against the same account" }
        }
        return response
    } else {
        val errorCode = response.extras.resultCodes?.transactionResultCode
        if (errorCode == "tx_bad_seq" && tries < maxTries) {
            // escalate how long it sleeps in between depending on the number of tries and randomize how long it sleeps
            // using increments of 1s because stellar transactions are relatively slow
            Thread.sleep(RandomUtils.nextLong(100, 1000*(tries.toLong() + 1)))
            return doTransactionInternal(tries + 1, maxTries, keyPair, transactionBlock)
        } else {
            val operationsFailures = response.extras.resultCodes?.operationsResultCodes?.joinToString(", ")
            throw IllegalStateException(
                "failure after $tries transaction failed $errorCode - $operationsFailures")
        }
    }
}

fun AccountResponse.Balance.balanceAmount(): Double { return balance.toDouble() }