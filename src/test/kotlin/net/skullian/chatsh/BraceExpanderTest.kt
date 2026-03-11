package net.skullian.chatsh

import net.skullian.chatsh.expansion.data.ExpansionCtx
import net.skullian.chatsh.expansion.impl.BraceExpander
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
class BraceExpanderTest {
    private val ctx = ExpansionCtx()

    private fun expand(s: String) = BraceExpander.expand(listOf(s), ctx)

    // comma lists

    @Test
    fun `simple comma list`() {
        assertEquals(listOf("a", "b", "c"), expand("{a,b,c}"))
    }

    @Test
    fun `comma list with prefix`() {
        assertEquals(listOf("preA", "preB"), expand("pre{A,B}"))
    }

    @Test
    fun `comma list with suffix`() {
        assertEquals(listOf("Asuf", "Bsuf"), expand("{A,B}suf"))
    }

    @Test
    fun `comma list with prefix and suffix`() {
        assertEquals(listOf("preAsuf", "preBsuf"), expand("pre{A,B}suf"))
    }

    @Test
    fun `npc remove example from brief`() {
        val result = expand("/npc remove {1,2,3,4}")
        assertEquals(listOf(
            "/npc remove 1",
            "/npc remove 2",
            "/npc remove 3",
            "/npc remove 4"
        ), result)
    }

    @Test
    fun `multiple brace pairs cross-product`() {
        val result = expand("{a,b}_{x,y}")
        assertEquals(listOf("a_x", "a_y", "b_x", "b_y"), result)
    }

    @Test
    fun `nested braces`() {
        val result = expand("{a,{b,c}}")
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `single item brace - pass through`() {
        assertEquals(listOf("{a}"), expand("{a}"))
    }

    @Test
    fun `unmatched brace - pass through`() {
        assertEquals(listOf("{a,b"), expand("{a,b"))
    }

    // int ranges

    @Test
    fun `simple ascending range`() {
        assertEquals(listOf("1", "2", "3", "4", "5"), expand("{1..5}"))
    }

    @Test
    fun `descending range`() {
        assertEquals(listOf("5", "4", "3", "2", "1"), expand("{5..1}"))
    }

    @Test
    fun `range with step`() {
        assertEquals(listOf("0", "2", "4", "6", "8", "10"), expand("{0..10..2}"))
    }

    @Test
    fun `range in command`() {
        val result = expand("/tp @s ~ ~ ~{-5..5..5}")
        assertEquals(listOf("/tp @s ~ ~ ~-5", "/tp @s ~ ~ ~0", "/tp @s ~ ~ ~5"), result)
    }

    @Test
    fun `zero-padded range`() {
        assertEquals(listOf("01", "02", "03"), expand("{01..03}"))
    }

    // char ranges

    @Test
    fun `char range ascending`() {
        assertEquals(listOf("a", "b", "c", "d", "e"), expand("{a..e}"))
    }

    @Test
    fun `char range descending`() {
        assertEquals(listOf("e", "d", "c", "b", "a"), expand("{e..a}"))
    }

    // falses

    @Test
    fun `plain string unchanged`() {
        assertEquals(listOf("/say hello world"), expand("/say hello world"))
    }

    @Test
    fun `empty string unchanged`() {
        assertEquals(listOf(""), expand(""))
    }
}
