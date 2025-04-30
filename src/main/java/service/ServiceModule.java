// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package service;

import annotations.service.Service;
import dependency_injector.DependencyInjector;
import java.util.Set;


public class ServiceModule {
    public static void registerServices(String basePackage) throws Exception {
        Set<Class<?>> scannedClasses = DependencyInjector.scanClasses(basePackage);

        for(Class<?> clazz : scannedClasses) {
            if(clazz.isAnnotationPresent(Service.class)) {
                DependencyInjector.register(clazz);
                System.out.println("Service registered: " + clazz.getName());
            }
        }
    }
}
