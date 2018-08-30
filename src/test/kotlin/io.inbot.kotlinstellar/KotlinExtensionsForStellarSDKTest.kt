package io.inbot.kotlinstellar

import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import org.stellar.sdk.KeyPair
import java.net.URLDecoder

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

    @Test
    fun shouldVerifySignature() {
        // data=%7B%22sender%22%3A%22aldi%2AbankA.com%22%2C%22need_info%22%3Atrue%2C%22tx%22%3A%22AAAAAEhAArfpmUJYq%2FQ9SFAH3YDzNLJEBI9i9TXmJ7s608xbAAAAZAAMon0AAAAJAAAAAAAAAAPUg1%2FwDrMDozn8yfiCA8LLC0wF10q5n5lo0GiFQXpPsAAAAAEAAAAAAAAAAQAAAADdvkoXq6TXDV9IpguvNHyAXaUH4AcCLqhToJpaG6cCyQAAAAAAAAAAAJiWgAAAAAA%3D%22%2C%22attachment%22%3A%22%7B%5C%22nonce%5C%22%3A%5C%221488805458327055805%5C%22%2C%5C%22transaction%5C%22%3A%7B%5C%22sender_info%5C%22%3A%7B%5C%22address%5C%22%3A%5C%22678+Mission+St%5C%22%2C%5C%22city%5C%22%3A%5C%22San+Francisco%5C%22%2C%5C%22country%5C%22%3A%5C%22US%5C%22%2C%5C%22first_name%5C%22%3A%5C%22Aldi%5C%22%2C%5C%22last_name%5C%22%3A%5C%22Dobbs%5C%22%7D%2C%5C%22route%5C%22%3A%5C%221%5C%22%2C%5C%22note%5C%22%3A%5C%22%5C%22%2C%5C%22extra%5C%22%3A%5C%22%5C%22%7D%2C%5C%22operations%5C%22%3Anull%7D%22%7D&sig=KgvyQTZsZQoaMy8jdwCUfLayfgfFMUdZJ%2B0BIvEwiH5aJhBXvhV%2BipRok1asjSCUS%2FUaGeGKDoizS1%2BtFiiyAA%3D%3D

        val data = URLDecoder.decode("%7B%22sender%22%3A%22aldi%2AbankA.com%22%2C%22need_info%22%3Atrue%2C%22tx%22%3A%22AAAAAEhAArfpmUJYq%2FQ9SFAH3YDzNLJEBI9i9TXmJ7s608xbAAAAZAAMon0AAAAJAAAAAAAAAAPUg1%2FwDrMDozn8yfiCA8LLC0wF10q5n5lo0GiFQXpPsAAAAAEAAAAAAAAAAQAAAADdvkoXq6TXDV9IpguvNHyAXaUH4AcCLqhToJpaG6cCyQAAAAAAAAAAAJiWgAAAAAA%3D%22%2C%22attachment%22%3A%22%7B%5C%22nonce%5C%22%3A%5C%221488805458327055805%5C%22%2C%5C%22transaction%5C%22%3A%7B%5C%22sender_info%5C%22%3A%7B%5C%22address%5C%22%3A%5C%22678+Mission+St%5C%22%2C%5C%22city%5C%22%3A%5C%22San+Francisco%5C%22%2C%5C%22country%5C%22%3A%5C%22US%5C%22%2C%5C%22first_name%5C%22%3A%5C%22Aldi%5C%22%2C%5C%22last_name%5C%22%3A%5C%22Dobbs%5C%22%7D%2C%5C%22route%5C%22%3A%5C%221%5C%22%2C%5C%22note%5C%22%3A%5C%22%5C%22%2C%5C%22extra%5C%22%3A%5C%22%5C%22%7D%2C%5C%22operations%5C%22%3Anull%7D%22%7D", "utf-8")

        val key = KeyPair.random()
        val sig = key.sign(data)
        key.toPublicPair().verify(data, sig) shouldBe true
    }
}