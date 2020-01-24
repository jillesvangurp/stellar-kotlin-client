[stellar-kotlin-client](../index.md) / [io.inbot.kotlinstellar](index.md) / [xdrEncode](./xdr-encode.md)

# xdrEncode

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> xdrEncode(xdr: T): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)

Encode an XDR instance to a base 64 encoded string.

### Parameters

`xdr` - an instance of an xdr class; must have a static encode(XdrDataOutputStream,String) method.