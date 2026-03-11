package net.skullian.zenith.core.flavor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service classes can annotate a single method with the <code>@Close</code>
 * annotation, which will be invoked when the service is disposed.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Close {
}
