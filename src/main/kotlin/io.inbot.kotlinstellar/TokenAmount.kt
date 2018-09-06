package io.inbot.kotlinstellar

import com.google.common.math.LongMath
import org.apache.commons.lang3.StringUtils
import org.stellar.sdk.Asset
import org.stellar.sdk.assetCode
import java.math.MathContext
import java.util.Locale

private val stroopsPerToken = LongMath.pow(10, 7)
private val maxTokens = Long.MAX_VALUE / stroopsPerToken

fun amount(amount: Double, asset: Asset? = null): TokenAmount {
    return TokenAmount.of(amount, asset)
}

fun amount(tokens: Long, stroops: Long, asset: Asset? = null): TokenAmount {
    return TokenAmount.of(tokens, stroops, asset)
}

fun amount(tokens: Long, asset: Asset? = null, stroops: Long = 0): TokenAmount {
    return TokenAmount.of(tokens, stroops, asset)
}

fun amount(tokens: String, asset: Asset? = null): TokenAmount {
    return TokenAmount.of(tokens, asset)
}

/**
Represents amounts in Stellar. Stellar uses 64 bit longs to store values. To fake decimals, they use stroops, wich is
 */
data class TokenAmount private constructor(val tokens: Long, val stroops: Long, val asset: Asset?) {

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
        if (stroops>Long.MAX_VALUE - tokens * stroopsPerToken) {
            throw IllegalArgumentException("$totalStroops is outside the maximum supported range of ${Long.MAX_VALUE}")
        }
    }

    fun divide(by: TokenAmount): TokenAmount {
        val divided = this.totalStroops.toBigDecimal()
            .divide(by.totalStroops.toBigDecimal(), MathContext.DECIMAL128).toDouble()
        return TokenAmount.of(divided)
    }

    fun inverse(): TokenAmount {
        val inverse = stroopsPerToken.toBigDecimal()
            .divide(this.totalStroops.toBigDecimal(), MathContext.DECIMAL128).toDouble()
        return TokenAmount.of(inverse)
    }

    val amount: String by lazy {
        "$tokens.${"%07d".format(Locale.ROOT, stroops)}"
    }

    override fun toString(): String {
        return "$amount${if (asset != null) " ${asset.assetCode}" else ""}"
    }

    operator fun compareTo(other: TokenAmount): Int {
        return totalStroops.compareTo(other.totalStroops)
    }

    companion object {
        fun of(amount: String, asset: Asset? = null): TokenAmount {
            val re = Regex("([0-9]+)(\\.([0-9]+))?")
            val match = re.matchEntire(amount)
            if (match == null) {
                throw IllegalArgumentException("malformed amount $amount should be xxxx.yyyyyyy")
            } else {
                val tokens = match.groupValues[1].toLong()
                val stroops = if (StringUtils.isNotBlank(match.groupValues[3])) match.groupValues[3].toLong() else 0

                return TokenAmount(tokens, stroops, asset)
            }
        }

        fun of(tokens: Long, stroops: Long = 0, asset: Asset? = null): TokenAmount {
            return TokenAmount(tokens, stroops, asset)
        }

        fun ofStroops(totalStroops: Long, asset: Asset? = null): TokenAmount {
            val tokens = totalStroops / stroopsPerToken
            val stroops = totalStroops % stroopsPerToken
            return of(tokens, stroops, asset)
        }

        fun of(amount: Double, asset: Asset? = null): TokenAmount {
            val tokens = (amount * stroopsPerToken).toLong() / stroopsPerToken
            val stroops = (amount * stroopsPerToken).toLong() % stroopsPerToken
            return TokenAmount(tokens, stroops, asset)
        }

        val maxAmount by lazy {
            ofStroops(Long.MAX_VALUE)
        }
    }
}