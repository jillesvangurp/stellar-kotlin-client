[inbot-stellar-kotlin-wrapper](../index.md) / [io.inbot.kotlinstellar](./index.md)

## Package io.inbot.kotlinstellar

The `KotlinStellarWrapper` and supporting classes.

### Types

| Name | Summary |
|---|---|
| [KotlinStellarWrapper](-kotlin-stellar-wrapper/index.md) | `class KotlinStellarWrapper`<br>Helper that makes doing common operations against Stellar less boiler plate heavy. |
| [StellarNetwork](-stellar-network/index.md) | `enum class StellarNetwork` |
| [TokenAmount](-token-amount/index.md) | `data class TokenAmount`<br>Represents amounts in Stellar. Stellar uses 64 bit longs to store values. To fake decimals, they use stroops, wich is |
| [TradeAggregationResolution](-trade-aggregation-resolution/index.md) | `enum class TradeAggregationResolution` |

### Properties

| Name | Summary |
|---|---|
| [nativeXlmAsset](native-xlm-asset.md) | `val nativeXlmAsset: AssetTypeNative` |

### Functions

| Name | Summary |
|---|---|
| [tokenAmount](token-amount.md) | `fun tokenAmount(amount: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, asset: Asset? = null): `[`TokenAmount`](-token-amount/index.md)<br>`fun tokenAmount(tokens: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, stroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 0, asset: Asset? = null): `[`TokenAmount`](-token-amount/index.md)<br>`fun tokenAmount(tokens: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, asset: Asset? = null): `[`TokenAmount`](-token-amount/index.md) |
| [xdrDecodeString](xdr-decode-string.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> xdrDecodeString(encoded: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](xdr-decode-string.md#T)`>): `[`T`](xdr-decode-string.md#T)<br>Decode an XDR string using the XDR class. |
| [xdrEncode](xdr-encode.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> xdrEncode(xdr: `[`T`](xdr-encode.md#T)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Encode an XDR instance to a base 64 encoded string. |
