package domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AmountTest {
    @Test
    fun `that we can create an amount and extract its string representations`() {
        val amount = Amount(100, Currency.USD)
        assertEquals("1.00 USD", amount.toString())
        assertEquals("1.00", amount.stringAmount)
    }

    @Test
    fun `that we can add two amounts`() {
        val amount1 = Amount(100, Currency.USD)
        val amount2 = Amount(200, Currency.USD)
        val result = amount1 + amount2
        assertEquals("3.00 USD", result.toString())
    }

    @Test
    fun `that we can subtract two amounts`() {
        val amount1 = Amount(200, Currency.USD)
        val amount2 = Amount(100, Currency.USD)
        val result = amount1 - amount2
        assertEquals("1.00 USD", result.toString())
    }

    @Test
    fun `that adding two difference currencies fails`() {
        val amount1 = Amount(100, Currency.USD)
        val amount2 = Amount(200, Currency.GBP)
        assertFailsWith<IllegalArgumentException> {
            amount1 + amount2
        }
    }

    @Test
    fun `that we can multiple an amount by an int`() {
        val amount = Amount(100, Currency.USD)
        val result = amount * 2
        assertEquals("2.00 USD", result.toString())
    }

    @Test
    fun `that we can divide an amount by an int`() {
        val amount = Amount(100, Currency.USD)
        val result = amount / 2
        assertEquals("0.50 USD", result.toString())
    }
}
