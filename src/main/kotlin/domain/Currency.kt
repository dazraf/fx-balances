package domain

/**
 * The enumeration of known currencies.
 *  most systems will represent these in a database table
 *  here, we'll just represent these as enums as a typesafe convenience
 */
enum class Currency(val code: String, val precision: Int = 2) {
    EUR("EUR"),
    GBP("GBP"),
    JPY("JPY", 0),
    USD("USD");

    override fun toString() = code
}

val Currency.zero get() = Amount(0, this)
