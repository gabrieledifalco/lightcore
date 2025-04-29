// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package controller;

import annotations.controller.RequestMapping;
import annotations.controller.RestController;
import annotations.web.DeleteMapping;
import annotations.web.GetMapping;
import annotations.web.PostMapping;
import annotations.web.PutMapping;
import dependency_injector.DependencyInjector;
import router.Router;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;

public class RestControllerModule {
    private static final Logger logger = Logger.getLogger(RestControllerModule.class.getName());

    /**
     * Registers all the routes from classes annotated with @RestController in the given package.
     * Scans the package for controllers and registers GET and POST routes based on annotations.
     *
     * @param basePackage The base package to scan for controllers.
     * @throws Exception If there is an error during the reflection or registration process.
     */
    public static void registerRoutes(String basePackage) throws Exception {
        Set<Class<?>> scannedClasses = DependencyInjector.scanClasses(basePackage);
        logger.info("Scanning classes in package: " + basePackage);
        logger.info("Found " + scannedClasses.size() + " classes.");

        for (Class<?> clazz : scannedClasses) {
            if (clazz.isAnnotationPresent(RestController.class)) {
                logger.info("Found RestController: " + clazz.getName());
                Object controllerInstance = DependencyInjector.getInstance(clazz);

                if(!clazz.isAnnotationPresent(RequestMapping.class)) {
                    logger.info("Missing @RequestMapping on controller: " + clazz);
                    throw new Exception();
                }

                String basePath = clazz.getAnnotation(RequestMapping.class).value();

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        String path = basePath + method.getAnnotation(GetMapping.class).value();
                        logger.info("Registering GET route: " + path);
                        Router.registerRoute("GET", path, controllerInstance, method);
                    }

                    if (method.isAnnotationPresent(PostMapping.class)) {
                        String path = basePath + method.getAnnotation(PostMapping.class).value();
                        logger.info("Registering POST route: " + path);
                        Router.registerRoute("POST", path, controllerInstance, method);
                    }

                    if(method.isAnnotationPresent(PutMapping.class)) {
                        String path = basePath + method.getAnnotation(PutMapping.class).value();
                        logger.info("Registering PUT route: " + path);
                        Router.registerRoute("PUT", path, controllerInstance, method);
                    }

                    if(method.isAnnotationPresent(DeleteMapping.class)) {
                        String path = basePath + method.getAnnotation(DeleteMapping.class).value();
                        logger.info("Registering DELETE route: " + path);
                        Router.registerRoute("DELETE", path, controllerInstance, method);
                    }
                }
            }
        }
    }
}