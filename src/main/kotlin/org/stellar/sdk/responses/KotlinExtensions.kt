package org.stellar.sdk.responses

import io.inbot.kotlinstellar.TokenAmount
import io.inbot.kotlinstellar.amount
import io.inbot.kotlinstellar.xdrDecodeString
import org.stellar.sdk.Asset
import org.stellar.sdk.assetCode
import org.stellar.sdk.xdr.TransactionResult

/**
 * Return a human readable multi line report with relevant details about the account.
 */
fun AccountResponse.describe(): String {
    return """
accountId: ${keypair.accountId} subEntryCount: $subentryCount home domain: $homeDomain

Balances:
${balances.map { "balance ${it.asset.assetCode} ${it.balance} ${it.limit}" }
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
fun AccountResponse.balanceFor(asset: Asset): TokenAmount {
    val balance = balanceMap()[asset]
    return if (balance != null) {
        balance.tokenAmount()
    } else {
        amount(0, asset)
    }
}

fun AccountResponse.Balance.tokenAmount(): TokenAmount {
    return amount(balance, asset)
}


fun AccountResponse.Balance.balanceAmount(): TokenAmount {
    return TokenAmount.of(balance)
}

fun SubmitTransactionResponse.getTransactionResult(): TransactionResult {
    return xdrDecodeString(resultXdr, TransactionResult::class)
}

fun SubmitTransactionResponse.describe(): String {
    val transactionResult = getTransactionResult()
    val fee = transactionResult.feeCharged.int64 ?: 0
    return """$ledger $hash success:$isSuccess fee:$fee ${transactionResult.result.results.map { it.tr.discriminant.name + " " }
        .joinToString(",")} ${extras?.resultCodes?.operationsResultCodes?.joinToString(",") ?: ""}"""
}