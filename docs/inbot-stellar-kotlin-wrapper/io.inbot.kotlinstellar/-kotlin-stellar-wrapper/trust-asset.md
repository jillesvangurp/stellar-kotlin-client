[inbot-stellar-kotlin-wrapper](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [KotlinStellarWrapper](index.md) / [trustAsset](./trust-asset.md)

# trustAsset

`fun trustAsset(account: KeyPair, asset: Asset, maxTrustedAmount: `[`TokenAmount`](../-token-amount/index.md)`, maxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = defaultMaxTries, signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair> = arrayOf(account)): SubmitTransactionResponse`

Create a trustline to an asset.

### Parameters

`account` - account that trusts the asset

`asset` - the asset

`maxTrustedAmount` - maximum amount to which you trust the asset

`maxTries` - maximum amount of times to retry the transaction in case of conflicst; default is 3

`signers` - list of signers, defaults to using the account key pair

**Return**
transaction response

