[inbot-stellar-kotlin-wrapper](../index.md) / [org.stellar.sdk](./index.md)

## Package org.stellar.sdk

Misc. extension functions to add functionality to the SDK.

### Properties

| Name | Summary |
|---|---|
| [assetCode](asset-code.md) | `val Asset.assetCode: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>asset code as a string (4 or 12 letter) or XLM if it is native. |
| [assetIssuer](asset-issuer.md) | `val Asset.assetIssuer: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |

### Functions

| Name | Summary |
|---|---|
| [amount](amount.md) | `fun Asset.amount(value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`TokenAmount`](../io.inbot.kotlinstellar/-token-amount/index.md)<br>`fun Asset.amount(value: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`TokenAmount`](../io.inbot.kotlinstellar/-token-amount/index.md)<br>`fun Asset.amount(tokens: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, stroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 0): `[`TokenAmount`](../io.inbot.kotlinstellar/-token-amount/index.md) |
| [buildAndSign](build-and-sign.md) | `fun Builder.buildAndSign(vararg pairs: KeyPair): Transaction`<br>Combine building and signing into a single call. |
| [describe](describe.md) | `fun Asset.describe(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [doTransaction](do-transaction.md) | `fun Server.doTransaction(network: Network, forAccount: KeyPair, maxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair> = arrayOf(forAccount), transactionTimeout: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 15000, baseFee: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, sequenceNumberOverride: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, transactionBlock: Builder.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): SubmitTransactionResponse`<br>Kotlin helper to create, sign, and submit a transaction. |
| [findAccount](find-account.md) | `fun Server.findAccount(pair: KeyPair): AccountResponse?` |
| [isNative](is-native.md) | `fun Asset.isNative(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [parseKeyPair](parse-key-pair.md) | `fun parseKeyPair(str: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): KeyPair?` |
| [rate](rate.md) | `fun Price.rate(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [seedString](seed-string.md) | `fun KeyPair.seedString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [sign](sign.md) | `fun KeyPair.sign(data: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [toPublicPair](to-public-pair.md) | `fun KeyPair.toPublicPair(): KeyPair` |
| [validateCanSign](validate-can-sign.md) | `fun KeyPair.validateCanSign(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [verify](verify.md) | `fun KeyPair.verify(data: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, base64Signature: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
