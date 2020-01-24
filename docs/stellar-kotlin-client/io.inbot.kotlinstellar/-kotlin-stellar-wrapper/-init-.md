[stellar-kotlin-client](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [KotlinStellarWrapper](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`KotlinStellarWrapper(server: Server, minimumBalance: `[`TokenAmount`](../-token-amount/index.md)` = TokenAmount.of(1, 0), defaultMaxTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 10, network: Network = Network("Standalone Network ; February 2017"))`

Helper that makes doing common operations against Stellar less boiler plate heavy.

### Parameters

`server` - the Stellar Server instance

`minimumBalance` - minimumBalance for accounts. Currently 20.0 on standalone chaines but cheaper in the public network.

`network` - Network instance of the network you are using. Network.TESTNET, Network.PUBLIC or a standalone network with the correct networkPassphrase. Defaults to a Network for "Standalone Network ; February 2017"

`defaultMaxTries` - default amount of times a transaction is retried in case of conflicts with the sequence number (tx_bad_seq) before failing; default is 10