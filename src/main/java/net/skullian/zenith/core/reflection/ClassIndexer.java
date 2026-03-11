package net.skullian.zenith.core.reflection;

import net.skullian.zenith.core.flavor.Flavor;
import net.skullian.zenith.core.flavor.exception.FlavorException;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.QueryFunction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is a class.
 *
 * @author Skullians
 * @since 11/03/2026
 */
public class ClassIndexer {
    private final Class<?> initializer;
    public final Reflections reflections;

    public ClassIndexer(Class<?> initializer, String basePackage) {
        this.initializer = initializer;

        ConfigurationBuilder config = new ConfigurationBuilder()
            .forPackage(basePackage, initializer.getClassLoader())
            .setParallel(true)
            .addScanners(
                Scanners.SubTypes,
                Scanners.TypesAnnotated,
                Scanners.MethodsAnnotated
            );
        this.reflections = new Reflections(config);
    }

    public <T> List<Class<?>> getSubTypes(Class<T> type) {
        return reflections
            .get(subTypes(type))
            .stream()
            .toList();
    }

    /**
     * Retrieves a list of methods annotated with the specified annotation within the configured reflections instance.
     *
     * @param <T> the type of the annotation
     * @param annotation the annotation class used to find annotated methods
     * @return a list of methods annotated with the specified annotation
     */
    public <T extends Annotation> List<Method> getMethodsAnnotatedWith(Class<T> annotation) {
        return reflections.get(annotated(annotation))
            .stream()
            .toList();
    }

    /**
     * Retrieves a list of classes that are annotated with the specified annotation.
     *
     * @param <T> the type of the annotation
     * @param annotation the annotation class to search for
     * @return a list of classes annotated with the specified annotation
     */
    public <T extends Annotation> List<Class<?>> getTypesAnnotatedWith(Class<T> annotation) {
        return reflections.get(Scanners.TypesAnnotated.with(annotation)).stream()
            .map(className -> {
                try {
                    return (Class<?>) Class.forName(className);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Invokes all methods annotated with the specified annotation within the context of the configured reflections instance.
     * If the method is static, it will be invoked without a target instance.
     * For non-static methods, an instance of the declaring class will be created using a singleton factory.
     * If any method fails to execute, a {@code FlavorException} is thrown.
     *
     * @param annotation the annotation class used to find and identify the methods to invoke
     *                   (must extend {@link Annotation})
     */
    public void invokeMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        getMethodsAnnotatedWith(annotation).forEach(method -> {
            try {
                Object target = Modifier.isStatic(method.getModifiers())
                    ? null
                    : Flavor.objectInstance(method.getDeclaringClass());

                method.setAccessible(true);
                method.invoke(target);
            } catch (Exception e) {
                throw new FlavorException("Failed to invoke method %s for class %s".formatted(method.getName(), method.getClass().getSimpleName()), e);
            }
        });
    }

    /**
     * Creates and returns a query function to search for methods annotated with the specified annotation.
     *
     * @param annotation the annotation class used to find annotated methods
     * @return a query function for retrieving methods annotated with the specified annotation
     */
    private <T> QueryFunction<Store, Method> annotated(Class<T> annotation) {
        return Scanners.MethodsAnnotated
            .with(annotation)
            .as(Method.class, initializer.getClassLoader());
    }

    /**
     * Finds and returns a query function to search for subtypes of classes
     * annotated with a specified annotation within the configured reflections instance.
     *
     * @param annotation the annotation class used to find subtypes
     * @return a query function for retrieving subtypes of classes annotated with the specified annotation
     */
    public <T> QueryFunction<Store, Class<?>> subTypes(Class<T> annotation) {
        return Scanners.SubTypes
            .with(annotation)
            .as();
    }
}
