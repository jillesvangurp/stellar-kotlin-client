package io.inbot.kotlinstellar

import com.google.common.math.LongMath
import org.apache.commons.lang3.StringUtils
import java.util.Locale

private val stroopsPerToken = LongMath.pow(10, 7)
private val maxTokens = Long.MAX_VALUE / stroopsPerToken

data class TokenAmount private constructor(val tokens: Long, val stroops: Long) {

    val totalStroops by lazy { tokens * stroopsPerToken + stroops }

    init {
        if (stroops < 0) {
            throw IllegalArgumentException("stroops should be positive, was $stroops")
        }
        if (stroops >= stroopsPerToken) {
            throw IllegalArgumentException("stroops should be < 10^7, was $stroops")
        }
        if (tokens < 0) {
            throw IllegalArgumentException("tokens should be positive, was $tokens")
        }
        if (tokens == maxTokens && stroops > 0) {
            throw IllegalArgumentException("$totalStroops is outside the maximum supported range of ${Long.MAX_VALUE}")
        }
    }

    override fun toString(): String {
        return "$tokens.${"%07d".format(Locale.ROOT,stroops)}"
    }

    operator fun compareTo(other: TokenAmount): Int {
        return totalStroops.compareTo(other.totalStroops)
    }

    companion object {
        @JvmStatic
        fun of(amount: String): TokenAmount {
            val re = Regex("([0-9]+)(\\.([0-9]+))?")
            val match = re.matchEntire(amount)
            if (match == null) {
                throw IllegalArgumentException("malformed amount should be xxxx.yyyyyyy")
            } else {
                val tokens = match.groupValues[1].toLong()
                val stroops = if (StringUtils.isNotBlank(match.groupValues[3])) match.groupValues[3].toLong() else 0

                return TokenAmount(tokens, stroops)
            }
        }

        fun of(tokens: Long, stroops: Long = 0): TokenAmount {
            return TokenAmount(tokens, stroops)
        }

        fun of(amount: Double): TokenAmount {
            val tokens = (amount * stroopsPerToken).toLong() / stroopsPerToken
            val stroops = (amount * stroopsPerToken).toLong() % stroopsPerToken
            return TokenAmount(tokens, stroops)
        }

        val maxAmount by lazy {
            TokenAmount(
                Long.MAX_VALUE / stroopsPerToken,
                0
            )
        }
    }
}