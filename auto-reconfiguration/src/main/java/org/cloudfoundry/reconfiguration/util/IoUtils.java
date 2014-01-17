/*
 * Copyright 2011-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.reconfiguration.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Utilities for IO-type objects
 */
public final class IoUtils {

    private IoUtils() {
    }

    /**
     * Close a collection of {@link Closeable}s
     *
     * @param closeables the collection of {@link Closeable}s to close
     */
    public static void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
