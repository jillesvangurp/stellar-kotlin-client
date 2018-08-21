package io.inbot.kotlinstellar

import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import org.stellar.sdk.KeyPair

class KotlinExtensionsForStellarSDKTest {
    @Test
    fun `should return seed as a string`() {
        KeyPair.random().seedString().length.shouldBeGreaterThan(0)
    }

    @Test
    fun `should throw illegal argument exception`() {
        shouldThrow<IllegalArgumentException> {
            KeyPair.fromAccountId(KeyPair.random().accountId).seedString()
        }
    }

    @Test
    fun `should recreate same account from seed stringf`() {
        val pair = KeyPair.random()

        val seed = pair.seedString()
        println(seed)

        val secondPair = KeyPair.fromSecretSeed(seed)

        pair.accountId shouldBe secondPair.accountId
    }
}