package io.inbot.kotlinstellar

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenAmountTest {
    @Test
    fun shouldFormatCorrectly() {
        TokenAmount(0, 0).toString() shouldBe "0.0000000"
        TokenAmount(10, 1).toString() shouldBe "10.0000001"
        TokenAmount(10, 666).toString() shouldBe "10.0000666"
        TokenAmount(1, 10.toBigInteger().pow(7).toLong() - 1).toString() shouldBe "1.9999999"
        TokenAmount(
            Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong() - 1,
            10.toBigInteger().pow(7).toLong() - 1
        ).toString() shouldBe "922337203684.9999999"
    }

    @Test
    fun `should check boundaries`() {
        assertThrows<IllegalArgumentException> {
            TokenAmount(-1, 0)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount(0, -1)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount(0, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount(1, 10.toBigInteger().pow(7).toLong())
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount(Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong(), 9999999)
        }
    }

    @Test
    fun shouldParseString() {
        TokenAmount.of("1.0000002").toString() shouldBe "1.0000002"
        TokenAmount.of("1").toString() shouldBe "1.0000000"
    }

}