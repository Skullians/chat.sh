package net.skullian.zenith.core.flavor.binder;

import net.skullian.zenith.core.flavor.annotation.inject.InjectScope;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FlavorBinder<T> {
    public final Class<T> clazz;
    public final Map<Class<? extends Annotation>, Predicate<? extends Annotation>> annotationChecks = new HashMap<>();

    public Object instance;
    public InjectScope scope = InjectScope.NO_SCOPE;

    public FlavorBinder(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Sets the InjectScope for the binder.
     * At this current point in time, the only scope is SINGLETON.
     *
     * @param scope The current {@link InjectScope} to set.
     * @return The current {@link FlavorBinder} instance.
     */
    public FlavorBinder<T> scoped(InjectScope scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Converts an instance to a {@link FlavorBinder}.
     *
     * @param object The instance to convert.
     * @return The converted {@link FlavorBinder}
     */
    public FlavorBinder<T> to(Object object) {
        this.instance = object;
        return this;
    }

    /**
     * Checks an annotation lambda, and converts it
     * into a {@link FlavorBinder}.
     */
    public <A extends Annotation> FlavorBinder<T> annotated(Class<A> annotation, Predicate<A> predicate) {
        this.annotationChecks.put(annotation, predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Predicate<Annotation> annotationCheck(Class<?> annotation) {
        return ((Predicate<Annotation>) annotationChecks.get(annotation));
    }
}
