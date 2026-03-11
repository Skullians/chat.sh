package net.skullian.zenith.core.flavor.binder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FlavorBinderMultiType {

    private final FlavorBinderContainer container;
    private final Object instance;

    private final List<Class<?>> types = new ArrayList<>();
    private Consumer<FlavorBinder<?>> binderInternalPopulator = b -> {};

    public FlavorBinderMultiType(FlavorBinderContainer container, Object instance) {
        this.container = container;
        this.instance = instance;
    }

    /**
     * Adds the provided type to the list of types to bind.
     *
     * @param type The type to add.
     * @return The current instance of {@link FlavorBinderMultiType}
     */
    public <T> FlavorBinderMultiType to(Class<T> type) {
        types.add(type);
        return this;
    }

    public FlavorBinderMultiType populate(Consumer<FlavorBinder<?>> consumer) {
        this.binderInternalPopulator = consumer;
        return this;
    }

    /**
     * Binds all instances to the provided types.
     */
    public void bind() {
        for (Class<?> type : types) {
            var binder = new FlavorBinder<>(type);
            binderInternalPopulator.accept(binder);
            container.getBinders().add(binder.to(instance));
        }
    }
}
