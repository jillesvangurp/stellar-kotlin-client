[inbot-stellar-kotlin-wrapper](../../index.md) / [io.inbot.kotlinstellar](../index.md) / [TokenAmount](./index.md)

# TokenAmount

`data class TokenAmount`

Represents amounts in Stellar. Stellar uses 64 bit longs to store values. To fake decimals, they use stroops, wich is

### Properties

| Name | Summary |
|---|---|
| [amount](amount.md) | `val amount: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [asset](asset.md) | `val asset: Asset?` |
| [bigDecimalValue](big-decimal-value.md) | `val bigDecimalValue: `[`BigDecimal`](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html) |
| [doubleValue](double-value.md) | `val doubleValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [stroops](stroops.md) | `val stroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [tokens](tokens.md) | `val tokens: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [totalStroops](total-stroops.md) | `val totalStroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |

### Functions

| Name | Summary |
|---|---|
| [compareTo](compare-to.md) | `operator fun compareTo(other: `[`TokenAmount`](./index.md)`): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [convert](convert.md) | `fun convert(price: Price, asset: Asset? = null): `[`TokenAmount`](./index.md) |
| [div](div.md) | `operator fun div(other: `[`TokenAmount`](./index.md)`): `[`TokenAmount`](./index.md) |
| [equals](equals.md) | `fun equals(other: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [inverse](inverse.md) | `fun inverse(): `[`TokenAmount`](./index.md) |
| [minus](minus.md) | `operator fun minus(other: `[`TokenAmount`](./index.md)`): `[`TokenAmount`](./index.md) |
| [plus](plus.md) | `operator fun plus(other: `[`TokenAmount`](./index.md)`): `[`TokenAmount`](./index.md) |
| [rem](rem.md) | `operator fun rem(other: `[`TokenAmount`](./index.md)`): `[`TokenAmount`](./index.md) |
| [times](times.md) | `operator fun times(other: `[`TokenAmount`](./index.md)`): `[`TokenAmount`](./index.md) |
| [toString](to-string.md) | `fun toString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Companion Object Properties

| Name | Summary |
|---|---|
| [maxAmount](max-amount.md) | `val maxAmount: `[`TokenAmount`](./index.md) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [of](of.md) | `fun of(amount: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, asset: Asset? = null): `[`TokenAmount`](./index.md)<br>`fun of(tokens: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, stroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 0, asset: Asset? = null): `[`TokenAmount`](./index.md)<br>`fun of(amount: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, asset: Asset? = null): `[`TokenAmount`](./index.md) |
| [ofStroops](of-stroops.md) | `fun ofStroops(totalStroops: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, asset: Asset? = null): `[`TokenAmount`](./index.md) |
