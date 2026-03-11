package net.skullian.zenith.core.flavor.annotation.inject;

import net.skullian.zenith.core.flavor.Flavor;

import java.lang.reflect.Field;
import java.util.function.Function;

public enum InjectScope {

    SINGLETON(clazz -> {
        return Flavor.objectInstance(clazz);
    }),

    NO_SCOPE(clazz -> null);

    public final Function<Class<?>, Object> instanceCreator;

    InjectScope(Function<Class<?>, Object> instanceCreator) {
        this.instanceCreator = instanceCreator;
    }
}
