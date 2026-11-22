package com.example.bursaryaid.feature.admin.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecisionFormTest {

    @Test
    fun plainNumber_isParsed() {
        assertEquals(40_000L, DecisionForm.parseAmount("40000"))
    }

    @Test
    fun commaSeparatedNumber_isParsed() {
        assertEquals(40_000L, DecisionForm.parseAmount("40,000"))
    }

    @Test
    fun spacesAroundAndInsideNumber_areIgnored() {
        assertEquals(40_000L, DecisionForm.parseAmount(" 40 000 "))
    }

    @Test
    fun emptyText_returnsNull() {
        assertNull(DecisionForm.parseAmount(""))
    }

    @Test
    fun letters_returnNull() {
        assertNull(DecisionForm.parseAmount("abc"))
    }

    @Test
    fun decimals_returnNull() {
        assertNull(DecisionForm.parseAmount("40.5"))
    }

    @Test
    fun negativeNumbers_returnNull() {
        assertNull(DecisionForm.parseAmount("-5"))
    }

    @Test
    fun zero_isParsed_andLeftToTheAllocationRulesToRefuse() {
        assertEquals(0L, DecisionForm.parseAmount("0"))
    }

    @Test
    fun numberTooBigToStore_returnsNull() {
        assertNull(DecisionForm.parseAmount("99999999999999999999"))
    }
}