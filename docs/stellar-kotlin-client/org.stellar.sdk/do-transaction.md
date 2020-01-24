[stellar-kotlin-client](../index.md) / [org.stellar.sdk](index.md) / [doTransaction](./do-transaction.md)

# doTransaction

`fun Server.doTransaction(network: Network, forAccount: KeyPair, maxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, signers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<KeyPair> = arrayOf(forAccount), transactionTimeout: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 15000, baseFee: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, sequenceNumberOverride: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, transactionBlock: Builder.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): SubmitTransactionResponse`

Kotlin helper to create, sign, and submit a transaction.

### Parameters

`forAccount` - account that signs the transaction

`maxTries` - the number of times to retry in case of a tx_bad_seq response.

`transactionBlock` - a block where you can add operations to the transaction

**Return**
the response

