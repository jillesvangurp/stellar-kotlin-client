package io.inbot.kotlinstellar

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.stellar.sdk.Price

class TokenAmountTest {
    @Test
    fun shouldFormatCorrectly() {
        tokenAmount(0, 0).toString() shouldBe "0.0000000"
        tokenAmount(10, 1).toString() shouldBe "10.0000001"
        tokenAmount(10, 666).toString() shouldBe "10.0000666"
        tokenAmount(1, 10.toBigInteger().pow(7).toLong() - 1).toString() shouldBe "1.9999999"
        tokenAmount(
            Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong() - 1,
            10.toBigInteger().pow(7).toLong() - 1
        ).toString() shouldBe "922337203684.9999999"
    }

    @Test
    fun `should check boundaries`() {
        assertThrows<IllegalArgumentException> {
            tokenAmount(-1, 0)
        }

        assertThrows<IllegalArgumentException> {
            tokenAmount(0, -1)
        }

        assertThrows<IllegalArgumentException> {
            tokenAmount(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            tokenAmount(0, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            tokenAmount(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            tokenAmount(1, 10.toBigInteger().pow(7).toLong())
        }

        assertThrows<IllegalArgumentException> {
            tokenAmount(Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong(), 9999999)
        }
    }

    @Test
    fun shouldParseString() {
        tokenAmount("1.0000002").toString() shouldBe "1.0000002"
        tokenAmount("1").toString() shouldBe "1.0000000"
    }

    @Test
    fun shouldCalculateRate() {
        tokenAmount(10).div(tokenAmount(20)) shouldBe tokenAmount(0.5)
        tokenAmount(10).div(tokenAmount(20)).inverse() shouldBe tokenAmount(2.0)
    }

    @Test
    fun shouldCorrectlyHandleMaxAmount() {
        // Long.MAX_VALUE == 9223372036854775807
        TokenAmount.maxAmount.toString() shouldBe "922337203685.4775807"
        tokenAmount(922337203685, 4775807)
        assertThrows<IllegalArgumentException> {
            // this would overflow
            tokenAmount(922337203685, 4775808)
        }
    }

    @Test
    fun `should do token math with operators`() {
        tokenAmount("0.1") + tokenAmount("1") shouldBe tokenAmount("1.1")
        var foo = tokenAmount("1")
        foo += tokenAmount("1")
        foo shouldBe tokenAmount("2")

        tokenAmount("2") - tokenAmount(2) shouldBe tokenAmount(0)
        tokenAmount("6") / tokenAmount(2) shouldBe tokenAmount(3)
        tokenAmount("5") % tokenAmount(2) shouldBe tokenAmount(1)
        tokenAmount("2") * tokenAmount(2) shouldBe tokenAmount(4)
    }

    @Test
    fun shouldConvert() {
        tokenAmount(6).convert(Price(1,3)).tokens shouldBe 2
    }
}