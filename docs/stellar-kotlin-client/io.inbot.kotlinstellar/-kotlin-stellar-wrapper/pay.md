[stellar-kotlin-client](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [KotlinStellarWrapper](index.md) / [pay](./pay.md)

# pay

`fun pay(sender: KeyPair, receiver: KeyPair, amount: `[`TokenAmount`](../-token-amount/index.md)`, asset: Asset = amount.asset ?: nativeXlmAsset, memo: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, maxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = defaultMaxTries, signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair> = arrayOf(sender), validate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): SubmitTransactionResponse`

Create a trustline to an asset.

### Parameters

`sender` - sender account

`receiver` - receiver account

`amount` - amount to be sent

`asset` - the asset

`memo` - optinoal text memo

`maxTries` - maximum amount of times to retry the transaction in case of conflicst; default is 3

`signers` - list of signers, defaults to using the account key pair

**Return**
transaction response

