package net.skullian.zenith.core.flavor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Flavor service.
 * These services are automatically detected and enabled/disposed
 * when your application starts.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * <code>#name()</code> is referenced in Flavor logging.
     * If this field is blank, the class name is used instead.
     *
     * @return The name of the service
     */
    String name() default "";

    /**
     * All Flavor services are sorted by their priority
     * when initialised.
     * <p>
     * By default, service priorities are set to 1.
     * If you depend on another service being
     * initialised before yours, ensure the service
     * priority is higher than the dependency.
     *
     * @return The initialisation priority of the service.
     */
    int priority() default 1;
}
