[inbot-stellar-kotlin-wrapper](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [KotlinStellarWrapper](index.md) / [createAccount](./create-account.md)

# createAccount

`fun createAccount(amountLumen: `[`TokenAmount`](../-token-amount/index.md)`, memo: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, sourceAccount: KeyPair? = null, newAccount: KeyPair = KeyPair.random(), maxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = defaultMaxTries, signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair> = if (sourceAccount != null) arrayOf(sourceAccount) else arrayOf(rootKeyPair)): KeyPair`

Create a new account and return the keyPair.

### Parameters

`opening` - balance. Needs to be &gt;= the minimum balance for your network.

`memo` - optional string memo

`maxTries` - maximum amount of times to retry the transaction in case of conflicst

`sourceAccount` - account that creates the newAccount. If null, the rootAccount will be used.

`newAccount` - account that will be created; defaults to a random key pair

**Return**
the key pair of the created account

