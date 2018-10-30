package io.inbot.kotlinstellar

import java.util.Locale

enum class TradeAggregationResolution(val resolution: Long) {
    //segment duration as millis since epoch. Supported values are 1 minute (60000), 5 minutes (300000), 15 minutes (900000), 1 hour (3600000), 1 day (86400000) and 1 week (604800000)
    T1_MINUTES(60000),
    T5_MINUTES(300000),
    T15_MINUTES(900000),
    T1_HOURS(3600000),
    T1_DAYS(86400000),
    T1_WEEKS(604800000)
    ;

    companion object {
        val validValues = TradeAggregationResolution.values()
            .map { it.name.replace("^T", "") }
            .joinToString(", ")

        fun parse(value: String): TradeAggregationResolution? {
            var NORMALIZED = value.toUpperCase(Locale.ROOT)
            if (!NORMALIZED.startsWith("T")) NORMALIZED = "T$NORMALIZED"
            if (!NORMALIZED.endsWith("S")) NORMALIZED = "${NORMALIZED}S"

            return try {
                TradeAggregationResolution.valueOf(NORMALIZED)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}