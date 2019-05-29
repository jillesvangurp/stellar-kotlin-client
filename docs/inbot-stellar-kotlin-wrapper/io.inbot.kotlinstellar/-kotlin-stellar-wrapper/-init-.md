[inbot-stellar-kotlin-wrapper](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [KotlinStellarWrapper](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`KotlinStellarWrapper(server: Server, networkPassphrase: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "Standalone Network ; February 2017", minimumBalance: `[`TokenAmount`](../-token-amount/index.md)` = TokenAmount.of(1, 0), defaultMaxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 10, stellarNetwork: `[`StellarNetwork`](../-stellar-network/index.md)` = StellarNetwork.standalone)`

Helper that makes doing common operations against Stellar less boiler plate heavy.

### Parameters

`server` - the Stellar Server instance

`networkPassphrase` - if using a standalone chain, provide the password here. This enables you to use the root account for account creation.

`minimumBalance` - minimumBalance for accounts. Currently 20.0 on standalone chaines but cheaper in the public network.

`defaultMaxTries` - default amount of times a transaction is retried in case of conflicts with the sequence number (tx_bad_seq) before failing; default is 10