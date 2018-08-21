package io.inbot.kotlinstellar

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenAmountTest {
    @Test
    fun shouldFormatCorrectly() {
        TokenAmount.of(0, 0).toString() shouldBe "0.0000000"
        TokenAmount.of(10, 1).toString() shouldBe "10.0000001"
        TokenAmount.of(10, 666).toString() shouldBe "10.0000666"
        TokenAmount.of(1, 10.toBigInteger().pow(7).toLong() - 1).toString() shouldBe "1.9999999"
        TokenAmount.of(
            Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong() - 1,
            10.toBigInteger().pow(7).toLong() - 1
        ).toString() shouldBe "922337203684.9999999"
    }

    @Test
    fun `should check boundaries`() {
        assertThrows<IllegalArgumentException> {
            TokenAmount.of(-1, 0)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount.of(0, -1)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount.of(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount.of(0, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount.of(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount.of(1, 10.toBigInteger().pow(7).toLong())
        }

        assertThrows<IllegalArgumentException> {
            TokenAmount.of(Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong(), 9999999)
        }
    }

    @Test
    fun shouldParseString() {
        TokenAmount.of("1.0000002").toString() shouldBe "1.0000002"
        TokenAmount.of("1").toString() shouldBe "1.0000000"
    }

}