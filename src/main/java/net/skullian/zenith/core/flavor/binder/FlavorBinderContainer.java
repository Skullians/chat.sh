package net.skullian.zenith.core.flavor.binder;

import java.util.ArrayList;
import java.util.List;

public abstract class FlavorBinderContainer {
    private final List<FlavorBinder<?>> binders = new ArrayList<>();

    /**
     * Used to populate the container with flavor binders.
     * You can override this and use the #bind method.
     * <p>
     * <pre>
     * {@code
     * private final JavaPlugin plugin = MyBukkitPlugin.getInstance();
     *
     * @Override
     * public void populate() {
     *     bind(plugin)
     *          .to(plugin.getClass())
     *          .to(JavaPlugin.class)
     *          .to(Plugin.class)
     *          .bind();
     *
     *     // or, make use of the Named annotation
     *     bind(plugin.getCurrentVersion())
     *          .to(String.class)
     *          .populate(it -> it.annotated(Named.class, a -> a.value().equals("pluginVersion")
     *          .bind();
     * }
     * }
     * </pre>
     */
    public abstract void populate();

    /**
     * Binds the provided object to a {@link FlavorBinderMultiType}.
     *
     * @param object The object to bind.
     * @return The multi-type Flavor binder.
     */
    public FlavorBinderMultiType bind(Object object) {
        return new FlavorBinderMultiType(this, object);
    }

    public List<FlavorBinder<?>> getBinders() {
        return binders;
    }
}
