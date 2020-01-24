[stellar-kotlin-client](../index.md) / [io.inbot.kotlinstellar](./index.md)

## Package io.inbot.kotlinstellar

The `KotlinStellarWrapper` and supporting classes.

### Types

| Name | Summary |
|---|---|
| [KotlinStellarWrapper](-kotlin-stellar-wrapper/index.md) | Helper that makes doing common operations against Stellar less boiler plate heavy.`class KotlinStellarWrapper` |
| [StellarNetwork](-stellar-network/index.md) | `enum class StellarNetwork` |
| [TokenAmount](-token-amount/index.md) | Represents amounts in Stellar. Stellar uses 64 bit longs to store values. To fake decimals, they use stroops, wich is`data class TokenAmount` |
| [TradeAggregationResolution](-trade-aggregation-resolution/index.md) | `enum class TradeAggregationResolution` |

### Properties

| Name | Summary |
|---|---|
| [nativeXlmAsset](native-xlm-asset.md) | `val nativeXlmAsset: AssetTypeNative` |

### Functions

| Name | Summary |
|---|---|
| [tokenAmount](token-amount.md) | `fun tokenAmount(amount: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, asset: Asset? = null): `[`TokenAmount`](-token-amount/index.md)<br>`fun tokenAmount(tokens: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, stroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 0, asset: Asset? = null): `[`TokenAmount`](-token-amount/index.md)<br>`fun tokenAmount(tokens: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, asset: Asset? = null): `[`TokenAmount`](-token-amount/index.md) |
| [xdrDecodeString](xdr-decode-string.md) | Decode an XDR string using the XDR class.`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> xdrDecodeString(encoded: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<T>): T` |
| [xdrEncode](xdr-encode.md) | Encode an XDR instance to a base 64 encoded string.`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> xdrEncode(xdr: T): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
