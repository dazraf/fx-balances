package domain

import java.util.*

/**
 * Represents an event that has occurred to an entity.
 */
sealed interface EntityEvent<T : Entity> {
    val time: BiTemporalTime

    /**
     * An event that has occurred to an entity.
     */
    data class ByValueEntityEvent<T : Entity>(val entity: T, override val time: BiTemporalTime) : EntityEvent<T> {
        val byRef get() = ByReferenceEntityEvent<T>(entity.id, time)
    }

    /**
     * References an event that has occurred to an entity.
     */
    data class ByReferenceEntityEvent<T : Entity>(val entityId: UUID, override val time: BiTemporalTime) :
        EntityEvent<T>
}

/**
 * Converts an entity to an event.
 */
fun <T : Entity> T.toEvent(time: BiTemporalTime = BiTemporalTime.now()) =
    EntityEvent.ByValueEntityEvent(this, time)
