package services

import domain.Entity
import domain.EntityEvent.ByValueEntityEvent
import java.util.*

interface EntityEventStoreWriter<T: Entity> {
    fun put(entityEvent: ByValueEntityEvent<T>)
}

interface EntityEventStoreReader<T: Entity> {
    fun getLatest(id: UUID): ByValueEntityEvent<T>
    fun getAll(id: UUID): List<ByValueEntityEvent<T>>
}

interface EntityEventStoreService<T: Entity>: EntityEventStoreReader<T>, EntityEventStoreWriter<T> {
    companion object {
        fun <T: Entity> createInMemory(): EntityEventStoreService<T> = InMemoryEntityEventStoreService()
    }
}

class InMemoryEntityEventStoreService<T: Entity>: EntityEventStoreService<T> {
    private val entities = mutableMapOf<UUID, MutableList<ByValueEntityEvent<T>>>()

    override fun put(entityEvent: ByValueEntityEvent<T>) {
        entities.computeIfAbsent(entityEvent.entity.id) { mutableListOf() }.add(entityEvent)
    }

    override fun getLatest(id: UUID): ByValueEntityEvent<T> {
        return entities[id]?.last() ?: throw IllegalArgumentException("Entity not found $id")
    }

    override fun getAll(id: UUID): List<ByValueEntityEvent<T>> {
        return entities[id] ?: throw IllegalArgumentException("Entities not found: $id")
    }
}
