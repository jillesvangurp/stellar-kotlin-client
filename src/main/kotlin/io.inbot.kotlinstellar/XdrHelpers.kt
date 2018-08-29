package io.inbot.kotlinstellar

import org.stellar.sdk.xdr.XdrDataInputStream
import org.stellar.sdk.xdr.XdrDataOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

/**
 * Decode an XDR string using the XDR class.
 * @param encoded the base64 encoded string
 * @param clazz the XDR class; must have a static decode(XdrDataInputStream) method
 * @return an instance of clazz
 */
inline fun <reified T : Any> xdrDecodeString(encoded: String, clazz: KClass<T>): T {
    val bytes = Base64.getDecoder().decode(encoded)
    val xdr = XdrDataInputStream(ByteArrayInputStream(bytes))

    val callable = clazz.members.find { it.name == "decode" }
    if (callable != null) {
        val result = callable.call(xdr)
        return clazz.cast(result)
    } else {
        throw IllegalStateException("cannot call decode(XdrDataInputStream)")
    }
}

/**
 * Encode an XDR instance to a base 64 encoded string.
 * @param xdr an instance of an xdr class; must have a static encode(XdrDataOutputStream,String) method.
 */
fun <T : Any> xdrEncode(xdr: T): String {
    val bos = ByteArrayOutputStream()
    val xdrDataOutputStream = XdrDataOutputStream(bos)

    val callable = xdr::class.members.find { it.name == "encode" }
    if (callable != null) {
        val result = callable.call(xdrDataOutputStream, xdr)
        if (result != null) {
            bos.flush()
            return Base64.getEncoder().encodeToString(bos.toByteArray())
        } else {
            throw IllegalStateException("encode(XdrDataOutputStream,String) returned null")
        }
    } else {
        throw IllegalStateException("cannot call encode(XdrDataOutputStream,String)")
    }
}