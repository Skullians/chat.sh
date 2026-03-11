package net.skullian.chatsh.util

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.concurrent.TimeUnit

object Adventure {

    private val miniMessage = MiniMessage.miniMessage()

    private val cache: Cache<String, Component> = Caffeine.newBuilder()
        .maximumSize(1_000)
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build()

    fun component(string: String): Component =
        cache.get(string) { miniMessage.deserialize(string) }

    fun component(string: String, vararg placeholders: TagResolver): Component {
        if (placeholders.isEmpty()) return component(string)
        return miniMessage.deserialize(string, *placeholders)
    }

    @JvmName("formatString")
    fun formatted(string: String, vararg placeholders: Pair<String, Any?>): Component {
        if (placeholders.isEmpty()) return component(string)

        val resolvers = placeholders.map { it.asResolver() }.toTypedArray()
        return miniMessage.deserialize(string, *resolvers)
    }

    val String.formatted: Component
        get() = component(this)

    fun String.formatted(vararg placeholders: Pair<String, Any?>): Component =
        formatted(this, *placeholders)

    fun Pair<String, Any?>.asResolver(): TagResolver {
        return when (val value = second) {
            is Component -> Placeholder.component(first, value)
            else -> Placeholder.parsed(first, value.toString())
        }
    }
}
