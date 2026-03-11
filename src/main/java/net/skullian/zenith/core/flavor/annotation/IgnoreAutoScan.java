package net.skullian.zenith.core.flavor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotating {@link Service} with <code>@IgnoreAutoScan</code> will prevent
 * that specific service from being automatically registered.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAutoScan {
}
