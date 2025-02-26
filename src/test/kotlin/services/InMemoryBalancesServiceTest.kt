package services

import domain.*
import domain.Currency.EUR
import domain.Currency.GBP
import domain.FxTrade.Status.CANCELLED
import services.FxBookingService.Companion.FX_TRADE_EVENT_TOPIC
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryBalancesServiceTest {

    @Test
    fun `that any currency balance is zero immediately after creation`() {
        // given
        val messagingService = MessagingService.createInMemory()
        val entityStore = EntityEventStoreService.createInMemory<FxTrade>()
        val balancesService = BalancesService.createInMemory(messagingService, entityStore)

        // then
        Currency.entries.forEach {
            val balance = balancesService.getBalance(it)
            assertEquals(0, balance.value)
        }
    }

    @Test
    fun `that balance is updated correctly after a trade event`() {
        // given
        val messagingService = MessagingService.createInMemory()
        val entityStore = EntityEventStoreService.createInMemory<FxTrade>()
        val balancesService = BalancesService.createInMemory(messagingService, entityStore)
        val trade = fxTradeEvent(Amount(10, EUR), Amount(20, GBP))

        // when
        messagingService.publish(FxBookingService.FX_TRADE_EVENT_TOPIC, trade)

        // then
        assertEquals(10, balancesService.getBalance(EUR).value)
        assertEquals(-20, balancesService.getBalance(GBP).value)
    }

    @Test
    fun `that after a trade event is cancelled the balances are reverted`() {
        // given
        val messagingService = MessagingService.createInMemory()
        val entityStore = EntityEventStoreService.createInMemory<FxTrade>()
        val balancesService = BalancesService.createInMemory(messagingService, entityStore)
        val tradeEvent = fxTradeEvent(Amount(10, EUR), Amount(20, GBP))

        messagingService.publish(FX_TRADE_EVENT_TOPIC, tradeEvent)
        assertEquals(10, balancesService.getBalance(EUR).value)
        assertEquals(-20, balancesService.getBalance(GBP).value)

        // when
        messagingService.publish(FX_TRADE_EVENT_TOPIC, tradeEvent.entity.copy(status = CANCELLED).toEvent())

        // then
        Currency.entries.forEach {
            val balance = balancesService.getBalance(it)
            assertEquals(0, balance.value)
        }
    }
}
