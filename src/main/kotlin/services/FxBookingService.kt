package services

import domain.Amount
import domain.FxTrade
import domain.toEvent
import services.FxBookingService.FxTradeRequest
import java.util.*

interface FxBookingService {
    data class FxTradeRequest(val buyAmount: Amount, val sellAmount: Amount)
    fun bookTrade(request: FxTradeRequest): FxTrade
    fun cancelTrade(tradeId: UUID): FxTrade

    companion object {
        // N.B. given more time, we would have separate topics for new trade and cancellation
        const val FX_TRADE_EVENT_TOPIC: String = "fx.trade.event"
        fun createInMemory(
            messagingService: MessagingService,
            eventStore: EntityEventStoreService<FxTrade>
        ): FxBookingService = InMemoryFxBookingService(messagingService, eventStore)
    }
}

class InMemoryFxBookingService(
    private val messagingService: MessagingService,
    private val eventStore: EntityEventStoreService<FxTrade>
) : FxBookingService {

    override fun bookTrade(request: FxTradeRequest): FxTrade {
        val trade = FxTrade(
            buyAmount = request.buyAmount,
            sellAmount = request.sellAmount
        )
        val event = trade.toEvent()
        eventStore.put(event)
        messagingService.publish(FxBookingService.FX_TRADE_EVENT_TOPIC, event)
        return trade
    }

    override fun cancelTrade(tradeId: UUID): FxTrade {
        val latestVersion = eventStore.getLatest(tradeId).entity
        val cancellationTrade = latestVersion.copy(status = FxTrade.Status.CANCELLED)
        val cancellationEvent = cancellationTrade.toEvent()
        eventStore.put(cancellationEvent)
        messagingService.publish(FxBookingService.FX_TRADE_EVENT_TOPIC, cancellationEvent.byRef)
        return cancellationTrade
    }
}

fun fxTradeRequest(buyAmount: Amount, sellAmount: Amount) = FxTradeRequest(buyAmount, sellAmount)
