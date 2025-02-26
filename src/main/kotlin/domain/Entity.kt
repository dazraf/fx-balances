package domain

import java.util.UUID

/**
 * An entity is any object that has a unique identifier
 */
interface Entity {
    val id: UUID
}
