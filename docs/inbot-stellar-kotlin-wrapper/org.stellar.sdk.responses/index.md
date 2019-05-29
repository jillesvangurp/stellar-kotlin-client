[inbot-stellar-kotlin-wrapper](../index.md) / [org.stellar.sdk.responses](./index.md)

## Package org.stellar.sdk.responses

### Functions

| Name | Summary |
|---|---|
| [balanceAmount](balance-amount.md) | `fun Balance.balanceAmount(): `[`TokenAmount`](../io.inbot.kotlinstellar/-token-amount/index.md) |
| [balanceFor](balance-for.md) | `fun AccountResponse.balanceFor(asset: Asset): Balance?`<br>Return the balance for a specific Asset. |
| [balanceMap](balance-map.md) | `fun AccountResponse.balanceMap(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<Asset, Balance>`<br>Return the balances as a map for easy lookups. |
| [describe](describe.md) | `fun AccountResponse.describe(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Return a human readable multi line report with relevant details about the account.`fun SubmitTransactionResponse.describe(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>`fun TradeResponse.describe(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>`fun TradeAggregationResponse.describe(baseAssetCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, counterAssetCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getTransactionResult](get-transaction-result.md) | `fun SubmitTransactionResponse.getTransactionResult(): TransactionResult` |
| [tokenAmount](token-amount.md) | `fun Balance.tokenAmount(): `[`TokenAmount`](../io.inbot.kotlinstellar/-token-amount/index.md) |
