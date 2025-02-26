package domain

import java.time.Instant

/**
 * Represents a point in time in the business and system time dimensions.
 */
data class BiTemporalTime(val businessTime: Instant, val systemTime: Instant) {
    companion object {
        fun now() = BiTemporalTime(Instant.now(), Instant.now())
    }
}
