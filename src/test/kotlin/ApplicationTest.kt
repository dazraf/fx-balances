import domain.Amount
import domain.Currency.*
import domain.FxTrade
import services.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `when a stream of fx trades have been booked then the balances are updated`() {
        // given
        val tradeStoreService = EntityEventStoreService.createInMemory<FxTrade>()
        val messagingService = MessagingService.createInMemory()
        val tradingService = FxBookingService.createInMemory(messagingService, tradeStoreService)
        val balancesService =
            BalancesService.createInMemory(messagingService, tradeStoreService) // only using the reader interface

        // when
        listOf(
            fxTradeRequest(Amount(10_00, EUR), Amount(20_00, GBP)),
            fxTradeRequest(Amount(10_00, GBP), Amount(15, JPY)),
            fxTradeRequest(Amount(15_00, GBP), Amount(5_00, EUR)),
        ).forEach {
            tradingService.bookTrade(it)
        }

        // then
        assertEquals(Amount(5_00, EUR), balancesService.getBalance(EUR))
        assertEquals(Amount(5_00, GBP), balancesService.getBalance(GBP))
        assertEquals(Amount(-15, JPY), balancesService.getBalance(JPY))
    }

    @Test
    fun `when a trade is cancelled the balances are updated`() {
        // given
        val tradeStoreService = EntityEventStoreService.createInMemory<FxTrade>()
        val messagingService = MessagingService.createInMemory()
        val tradingService = FxBookingService.createInMemory(messagingService, tradeStoreService)
        val balancesService =
            BalancesService.createInMemory(messagingService, tradeStoreService) // only using the reader interface
        val trades = listOf(
            fxTradeRequest(Amount(10_00, EUR), Amount(20_00, GBP)),
            fxTradeRequest(Amount(10_00, GBP), Amount(15, JPY)),
            fxTradeRequest(Amount(15_00, GBP), Amount(5_00, EUR)),
        ).map {
            tradingService.bookTrade(it)
        }
        assertEquals(Amount(5_00, EUR), balancesService.getBalance(EUR))
        assertEquals(Amount(5_00, GBP), balancesService.getBalance(GBP))
        assertEquals(Amount(-15, JPY), balancesService.getBalance(JPY))

        // when
        tradingService.cancelTrade(trades[0].id)

        // then
        assertEquals(Amount(-5_00, EUR), balancesService.getBalance(EUR))
        assertEquals(Amount(25_00, GBP), balancesService.getBalance(GBP))
        assertEquals(Amount(-15, JPY), balancesService.getBalance(JPY))
    }
}
