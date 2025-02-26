package domain

import java.time.Instant
import java.util.*

data class FxTrade(
    override val id: UUID = UUID.randomUUID(),
    val buyAmount: Amount,
    val sellAmount: Amount,
    val tradeDate: Instant = Instant.now(),
    val status: Status = Status.BOOKED
) : Entity {
    enum class Status {
        BOOKED,
        CANCELLED
    }
}

fun fxTrade(buyAmount: Amount, sellAmount: Amount) = FxTrade(buyAmount = buyAmount, sellAmount = sellAmount)
fun fxTradeEvent(buyAmount: Amount, sellAmount: Amount) = fxTrade(buyAmount, sellAmount).toEvent()
