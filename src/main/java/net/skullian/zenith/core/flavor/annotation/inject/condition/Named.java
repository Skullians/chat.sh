package net.skullian.zenith.core.flavor.annotation.inject.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Named annotation is used as a "condition" for dependency injection.
 * It ensures that the correct injected field or parameter is supplied.
 * <p>
 * You would use the <code>populate</code> function in a FlavorBinderContainer
 * to specify the named value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Named {
}
