[inbot-stellar-kotlin-wrapper](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [KotlinStellarWrapper](index.md) / [createAccount](./create-account.md)

# createAccount

`fun createAccount(amountLumen: `[`TokenAmount`](../-token-amount/index.md)`, memo: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, sourceAccount: KeyPair? = null, newAccount: KeyPair = KeyPair.random(), maxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = defaultMaxTries, signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair> = if (sourceAccount != null) arrayOf(sourceAccount) else arrayOf(rootKeyPair ?: throw IllegalStateException("signers are required if not running on a standalone net"))): KeyPair`

Create a new account and return the keyPair.

### Parameters

`amountLumen` - the token amount

`memo` - optional string memo

`sourceAccount` - account that creates the newAccount. If null, the root keypair of your standalone network will be used in case you are using a Standalone network.

`newAccount` - account that will be created; defaults to a random key pair

`maxTries` - maximum amount of times to retry the transaction in case of conflicst

`signers` - list of signers, defaults to using either the sourceAccount or the rootKeyPair in case you are using a standalone network. Not providing a sourceAccount if you are not on a standalone network results in an exception.

### Exceptions

`IllegalStateException` - if you don't provide a sourceAccount and are not on a standalone network where we can use the rootKeypair associated with the passphrase.

**Return**
the key pair of the created account

