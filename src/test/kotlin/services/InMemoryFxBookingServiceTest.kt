package services

import domain.*
import domain.Currency.EUR
import domain.EntityEvent.ByValueEntityEvent
import domain.FxTrade.Status.CANCELLED
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import services.FxBookingService.FxTradeRequest

class InMemoryFxBookingServiceTest {

    @Test
    fun `that given a booked trade the trade is available in the event store and that a message has been sent`() {
        // given
        val messagingService = MessagingService.createInMemory()
        val eventStore = EntityEventStoreService.createInMemory<FxTrade>()
        val fxBookingService = FxBookingService.createInMemory(messagingService, eventStore)

        var messagedEvent: ByValueEntityEvent<FxTrade>? = null

        messagingService.subscribe<FxTrade>(FxBookingService.FX_TRADE_EVENT_TOPIC) { message ->
            when (val event = message.entityEvent) {
                is ByValueEntityEvent -> {
                    messagedEvent = event
                }
                is EntityEvent.ByReferenceEntityEvent -> {
                    throw IllegalStateException("Should not have received a ByReferenceEntityEvent in this use-case")
                }
            }
        }

        val buyAmount = Amount(10, EUR)
        val sellAmount = Amount(20, Currency.GBP)

        // when
        val trade = fxBookingService.bookTrade(FxTradeRequest(buyAmount, sellAmount))

        // then
        val storedEvent = eventStore.getLatest(trade.id)
        assertEquals(trade, storedEvent.entity)
        assertEquals(trade, messagedEvent!!.entity)
    }

    @Test
    fun `that cancelling a trade creates a new entity in the entity event store and messages a by reference message`() {
        // given
        val messagingService = MessagingService.createInMemory()
        val eventStore = EntityEventStoreService.createInMemory<FxTrade>()
        val fxBookingService = FxBookingService.createInMemory(messagingService, eventStore)

        var messagedEvent: ByValueEntityEvent<FxTrade>? = null

        val buyAmount = Amount(10, EUR)
        val sellAmount = Amount(20, Currency.GBP)

        val trade = fxBookingService.bookTrade(FxTradeRequest(buyAmount, sellAmount))

        messagingService.subscribe<FxTrade>(FxBookingService.FX_TRADE_EVENT_TOPIC) { message ->
            when (val event = message.entityEvent) {
                is ByValueEntityEvent -> {
                    throw IllegalStateException("Should not have received a ByValueEntityEvent in this use-case")
                }
                is EntityEvent.ByReferenceEntityEvent -> {
                    val entityEvent = eventStore.getLatest(event.entityId)
                    messagedEvent = entityEvent
                }
            }
        }

        // when
        val cancellationTrade = fxBookingService.cancelTrade(trade.id)

        // then
        val storedEvent = eventStore.getLatest(trade.id)
        assertEquals(CANCELLED, storedEvent.entity.status)
        assertEquals(CANCELLED, cancellationTrade.status)
        assertEquals(trade.id, messagedEvent!!.entity.id)
    }
}
