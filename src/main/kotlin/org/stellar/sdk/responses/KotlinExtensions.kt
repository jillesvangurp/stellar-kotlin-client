package org.stellar.sdk.responses

import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.tokenAmount
import io.inbot.kotlinstellar.xdrDecodeString
import org.stellar.sdk.Asset
import org.stellar.sdk.assetCode
import org.stellar.sdk.describe
import org.stellar.sdk.xdr.TransactionResult

/**
 * Return a human readable multi line report with relevant details about the account.
 */
fun AccountResponse.describe(): String {
    return """
accountId: $accountId subEntryCount: $subentryCount home domain: $homeDomain

thresholds: ${thresholds.lowThreshold} ${thresholds.medThreshold} ${thresholds.highThreshold}
signers:
${signers.map { s -> "\t${s.key} ${s.weight}" }.joinToString("\n")}
authRequired: ${flags.authRequired}
authRevocable: ${flags.authRevocable}

Balances:
${balances.map {
        "${it.asset.describe()} b:${it.balance} l:${it.limit ?: "-"} - sl: ${it.sellingLiabilities
            ?: "-"} - bl: ${it.buyingLiabilities ?: "-"}"
    }
        .joinToString("\n")}
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
    val balance = balanceMap()[asset]
    return if (balance != null) {
        balance
    } else {
        null
    }
}

fun AccountResponse.Balance.tokenAmount(): TokenAmount {
    return tokenAmount(balance, asset)
}

fun AccountResponse.Balance.balanceAmount(): TokenAmount {
    return TokenAmount.of(balance)
}

fun SubmitTransactionResponse.getTransactionResult(): TransactionResult {
    return xdrDecodeString(resultXdr.orNull(), TransactionResult::class)
}

fun SubmitTransactionResponse.describe(): String {
    val transactionResult = getTransactionResult()
    val fee = transactionResult.feeCharged.int64 ?: 0
    val txResults = transactionResult.result?.results?.joinToString(",") { it.tr.discriminant.name + " " } ?: ""
    return """$ledger $hash success:$isSuccess fee:$fee $txResults ${extras?.resultCodes?.operationsResultCodes?.joinToString(",") ?: ""}"""
}

fun TradeResponse.describe(): String {
    return "$ledgerCloseTime $baseAccount sold $baseAmount ${baseAsset.assetCode} for $counterAmount ${counterAsset.assetCode} to $counterAccount at ${price.numerator}/${price.denominator} (${price.numerator.toDouble() / price.denominator.toDouble()})"
}

fun TradeAggregationResponse.describe(baseAssetCode: String, counterAssetCode: String): String {
    return "${date.toInstant()} O:$open C:$close $baseVolume $baseAssetCode to $counterVolume $counterAssetCode - L:$low A:$avg H:$high count:$tradeCount"
}
