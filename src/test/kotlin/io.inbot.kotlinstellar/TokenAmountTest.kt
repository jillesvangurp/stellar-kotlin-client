package io.inbot.kotlinstellar

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenAmountTest {
    @Test
    fun shouldFormatCorrectly() {
        amount(0, 0).toString() shouldBe "0.0000000"
        amount(10, 1).toString() shouldBe "10.0000001"
        amount(10, 666).toString() shouldBe "10.0000666"
        amount(1, 10.toBigInteger().pow(7).toLong() - 1).toString() shouldBe "1.9999999"
        amount(
            Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong() - 1,
            10.toBigInteger().pow(7).toLong() - 1
        ).toString() shouldBe "922337203684.9999999"
    }

    @Test
    fun `should check boundaries`() {
        assertThrows<IllegalArgumentException> {
            amount(-1, 0)
        }

        assertThrows<IllegalArgumentException> {
            amount(0, -1)
        }

        assertThrows<IllegalArgumentException> {
            amount(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            amount(0, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            amount(Long.MAX_VALUE, Long.MAX_VALUE)
        }

        assertThrows<IllegalArgumentException> {
            amount(1, 10.toBigInteger().pow(7).toLong())
        }

        assertThrows<IllegalArgumentException> {
            amount(Long.MAX_VALUE / 10.toBigInteger().pow(7).toLong(), 9999999)
        }
    }

    @Test
    fun shouldParseString() {
        amount("1.0000002").toString() shouldBe "1.0000002"
        amount("1").toString() shouldBe "1.0000000"
    }

    @Test
    fun shouldCalculateRate() {
        amount(10).divide(amount(20)) shouldBe amount(0.5)
        amount(10).divide(amount(20)).inverse() shouldBe amount(2.0)
    }

    @Test
    fun shouldCorrectlyHandleMaxAmount() {
        // Long.MAX_VALUE == 9223372036854775807
        TokenAmount.maxAmount.toString() shouldBe "922337203685.4775807"
        amount(922337203685, 4775807)
        assertThrows<IllegalArgumentException> {
            // this would overflow
            amount(922337203685, 4775808)
        }
    }
}