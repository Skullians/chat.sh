package net.skullian.chatsh

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.skullian.chatsh.dispatcher.CommandDispatcher
import net.skullian.chatsh.expansion.Expander
import net.skullian.chatsh.expansion.ExpansionRegistry
import net.skullian.chatsh.expansion.completion.ExpansionCompleter
import net.skullian.chatsh.expansion.data.ExpansionCtx
import net.skullian.zenith.core.flavor.Flavor
import net.skullian.zenith.core.flavor.FlavorOptions
import net.skullian.zenith.core.reflection.ClassIndexer
import org.slf4j.LoggerFactory

@Environment(EnvType.CLIENT)
object ChatSh : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("chat.sh")

    private lateinit var flavor: Flavor;
    private lateinit var classIndexer: ClassIndexer

    val rootContext = ExpansionCtx() // will persist later
    val rootDispatcher = CommandDispatcher(rootContext)
    val rootCompleter = ExpansionCompleter(rootContext)

    override fun onInitializeClient() {
        this.flavor = Flavor.create(
            this.javaClass,
            FlavorOptions(
                this.logger,
                this.javaClass.packageName
            )
        )
        this.classIndexer = flavor.reflections;

        this.flavor.customLoader(Expander::class.java) {
            ExpansionRegistry.register(it)
        }

        this.flavor.startup()
    }

    fun disable() {
        this.flavor.close()
    }
}
