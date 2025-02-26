package services

import domain.Entity
import domain.EntityEvent

data class EntityEventMessage<T : Entity>(val topic: String, val entityEvent: EntityEvent<T>)

interface MessagingService {
    fun <T : Entity> subscribe(topic: String, callback: (EntityEventMessage<T>) -> Unit)
    fun <T : Entity> publish(topic: String, entityEvent: EntityEvent<T>)

    companion object {
        fun createInMemory(): MessagingService = InMemoryMessagingService()
    }
}

class InMemoryMessagingService : MessagingService {
    private val subscribers = mutableMapOf<String, MutableList<(EntityEventMessage<Entity>) -> Unit>>()

    override fun <T : Entity> subscribe(topic: String, callback: (EntityEventMessage<T>) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        subscribers.computeIfAbsent(topic) { mutableListOf() }.add(callback as (EntityEventMessage<Entity>) -> Unit)
    }

    override fun <T : Entity> publish(topic: String, entityEvent: EntityEvent<T>) {
        val exceptions = mutableListOf<Exception>()
        @Suppress("UNCHECKED_CAST")
        subscribers[topic]?.forEach {
            try {
                it(EntityEventMessage(topic, entityEvent as EntityEvent<Entity>))
            } catch (err: Exception) {
                exceptions.add(err)
            }
        }
        if (exceptions.isNotEmpty()) {
            throw Exception(exceptions.map { it.message }.joinToString("\n"))
        }
    }
}
