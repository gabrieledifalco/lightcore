// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package dependency_injector;

import annotations.autowired.Autowired;
import annotations.component.Component;
import annotations.controller.RestController;
import annotations.repository.Repository;
import annotations.service.Service;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A lightweight dependency injection container for managing and injecting
 * instances of annotated classes (e.g., @Service, @Repository, @RestController, @Module).
 */
public class DependencyInjector {
    // Map to store created instances by type
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    /**
     * Registers a class in the container and creates its instance if not already present.
     *
     * @param clazz the class to register
     * @param <T>   the type of the class
     * @throws Exception if instantiation fails
     */
    public static <T> void register(Class<T> clazz) throws Exception {
        if (!instances.containsKey(clazz)) {
            T instance = createInstance(clazz);
            injectDependencies(instance);
            instances.put(clazz, instance);
        }
    }

    public static <T> void register(Class<T> clazz, Object instance) {
        if (!instances.containsKey(clazz)) {
            instances.put(clazz, instance);
        }
    }

    /**
     * Retrieves an instance of the given class from the container.
     *
     * @param clazz the class to retrieve
     * @return the registered instance, or null if not found
     */
    public static Object getInstance(Class<?> clazz) throws Exception {
        if(!instances.containsKey(clazz)) {
            register(clazz);
        }

        return instances.get(clazz);
    }

    /**
     * Creates a new instance of a class using its default constructor.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the class
     * @return a new instance of the class
     * @throws Exception if instantiation fails
     */
    public static <T> T createInstance(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Injects dependencies into the fields of the provided object,
     * by scanning for fields annotated with @Autowired.
     *
     * @param object the target object
     * @throws Exception if injection fails
     */
    public static void injectDependencies(Object object) throws Exception {
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Class<?> fieldType = field.getType();

                Object dependency = getInstance(fieldType);
                if (dependency == null) {
                    register(fieldType);
                    dependency = getInstance(fieldType);
                }

                field.setAccessible(true);
                field.set(object, dependency);
            }
        }
    }

    /**
     * Scans the given package and returns all classes found within it.
     *
     * @param packageName the base package name to scan
     * @return a set of all discovered classes in the package
     * @throws Exception placeholder for future implementation
     */
    public static Set<Class<?>> getClassesFromPackage(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(Object.class);
    }

    /**
     * Scans for classes annotated with supported dependency injection annotations
     * such as @Service, @Repository, @RestController, or @Module.
     *
     * @param basePackage the base package to scan
     * @return a set of classes annotated with supported annotations
     * @throws Exception if scanning fails
     */
    public static Set<Class<?>> scanClasses(String basePackage) {
        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        Set<Class<?>> all = new HashSet<>();

        all.addAll(reflections.getTypesAnnotatedWith(RestController.class));
        all.addAll(reflections.getTypesAnnotatedWith(Repository.class));
        all.addAll(reflections.getTypesAnnotatedWith(Service.class));
        all.addAll(reflections.getTypesAnnotatedWith(Component.class));

        return all;
    }
}