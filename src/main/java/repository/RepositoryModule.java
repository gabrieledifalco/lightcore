// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package repository;

import annotations.repository.Repository;
import controller.RestControllerModule;
import dependency_injector.DependencyInjector;

import java.util.Set;
import java.util.logging.Logger;

public class RepositoryModule {
    private static final Logger logger = Logger.getLogger(RestControllerModule.class.getName());

    public static void registerRepositories(String basePackage) throws Exception{
        Set<Class<?>> scannedClasses = DependencyInjector.scanClasses(basePackage);

        for(Class<?> clazz : scannedClasses) {
            if(clazz.isAnnotationPresent(Repository.class)) {
                Object proxy = RepositoryProxy.create(clazz);
                DependencyInjector.register(clazz, proxy);
            }
        }
    }
}
