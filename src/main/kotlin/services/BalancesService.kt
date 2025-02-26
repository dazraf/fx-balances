package services

import domain.*

interface BalancesService {
    val balances: Map<Currency, Amount>
    fun getBalance(currency: Currency): Amount

    companion object {
        fun createInMemory(
            messagingService: MessagingService,
            tradeStoreReader: EntityEventStoreReader<FxTrade>
        ): BalancesService = InMemoryBalancesService(messagingService, tradeStoreReader)
    }
}

class InMemoryBalancesService(
    messagingService: MessagingService,
    private val tradeStoreReader: EntityEventStoreReader<FxTrade>
) : BalancesService {
    override val balances = mutableMapOf<Currency, Amount>()

    init {
        // in a real system we would have lifecycle management of services which would management dependencies such as this
        messagingService.subscribe(FxBookingService.FX_TRADE_EVENT_TOPIC) { event ->
            processEvent(event.entityEvent)
        }
    }

    private fun processEvent(event: EntityEvent<FxTrade>) {
        when (event) {
            is EntityEvent.ByReferenceEntityEvent -> {
                val trade = tradeStoreReader.getLatest(event.entityId).entity
                processTrade(trade)
            }

            is EntityEvent.ByValueEntityEvent -> {
                processTrade(event.entity)
            }
        }
    }

    override fun getBalance(currency: Currency) = balances[currency] ?: currency.zero

    private fun processTrade(trade: FxTrade) {
        val cancellationFactor = if (trade.status == FxTrade.Status.CANCELLED) -1 else 1
        val buyAmount = trade.buyAmount * cancellationFactor
        val sellAmount = trade.sellAmount * cancellationFactor

        // there's potential for race conditions here if there are multiple threads of callers with contending currencies
        // in a real system, we'd use a transaction context (either in a database or a (distributed) in-memory cache)
        // which usually have efficient optimistic locking or lock-free implementations

        balances.compute(buyAmount.currency) { _, balance -> (balance ?: buyAmount.currency.zero) + buyAmount }
        balances.compute(sellAmount.currency) { _, balance -> (balance ?: sellAmount.currency.zero) - sellAmount }
    }
}
