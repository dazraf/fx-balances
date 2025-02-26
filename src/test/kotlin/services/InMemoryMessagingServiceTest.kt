package services

import domain.Amount
import domain.Currency.EUR
import domain.Currency.GBP
import domain.FxTrade
import domain.fxTradeEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InMemoryMessagingServiceTest {
    @Test
    fun `that we can subscribe and publish to a topic`() {
        val messagingService = MessagingService.createInMemory()
        val topic = "fx.trade.event"
        var receivedEvent: EntityEventMessage<FxTrade>? = null
        messagingService.subscribe(topic) { event ->
            receivedEvent = event
        }
        val event = fxTradeEvent(Amount(10, EUR), Amount(20, GBP))
        messagingService.publish(topic, event)
        assertNotNull(receivedEvent)
        assertEquals(event, receivedEvent?.entityEvent)
    }
}
