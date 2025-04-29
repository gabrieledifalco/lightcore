// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package annotations.nodejs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    String value();
}
