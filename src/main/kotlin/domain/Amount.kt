package domain

import kotlin.math.pow

/**
 * Represents an amount of money in a given currency.
 */
data class Amount(val value: Int, val currency: Currency) {
    val stringAmount
        get() = String.format(
            "%,.${currency.precision}f",
            value.toDouble() / 10.0.pow(currency.precision)
        )

    override fun toString() = "$stringAmount ${currency.code}"
}

operator fun Amount.plus(rhs: Amount): Amount {
    validateCompatibleAmount(rhs)
    return copy(value = value + rhs.value)
}

operator fun Amount.minus(rhs: Amount): Amount {
    validateCompatibleAmount(rhs)
    return copy(value = value - rhs.value)
}

operator fun Amount.unaryMinus(): Amount {
    return copy(value = -value)
}

operator fun Amount.times(rhs: Int): Amount {
    return copy(value = value * rhs)
}

operator fun Amount.div(rhs: Int): Amount {
    return copy(value = value / rhs)
}

private fun Amount.validateCompatibleAmount(rhs: Amount) {
    if (currency != rhs.currency) {
        throw IllegalArgumentException("cannot add different currency amounts. lhs is ${currency}. rhs is ${rhs.currency}")
    }
}
