[inbot-stellar-kotlin-wrapper](../index.md) / [io.inbot.kotlinstellar](index.md) / [xdrDecodeString](./xdr-decode-string.md)

# xdrDecodeString

`inline fun <reified T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> xdrDecodeString(encoded: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](xdr-decode-string.md#T)`>): `[`T`](xdr-decode-string.md#T)

Decode an XDR string using the XDR class.

### Parameters

`encoded` - the base64 encoded string

`clazz` - the XDR class; must have a static decode(XdrDataInputStream) method

**Return**
an instance of clazz

