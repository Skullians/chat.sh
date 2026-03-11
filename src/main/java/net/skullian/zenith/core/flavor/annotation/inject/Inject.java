package net.skullian.zenith.core.flavor.annotation.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similarly to <a href="https://github.com/google/guice">guice</a>, Flavor
 * provides means for dependency injection.
 * <p>
 * You can annotate fields or method parameters with <code>@Inject</code>,
 * and Flavor will facilitate the dependency injection, assuming it is
 * provided with the means to do so.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
