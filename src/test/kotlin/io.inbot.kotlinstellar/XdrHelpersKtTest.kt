package io.inbot.kotlinstellar

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.stellar.sdk.xdr.TransactionResult

internal class XdrHelpersKtTest {
    @Test
    fun shouldDecodeAndEncode() {
        val encoded = "AAAAAAAAAGQAAAAAAAAAAQAAAAAAAAABAAAAAAAAAAA="
        val tr = xdrDecodeString(encoded, TransactionResult::class)
        val reEncoded = xdrEncode(tr)
        reEncoded shouldBe encoded
    }
}
