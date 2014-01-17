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

/**
 * Utility class for identifying the type of a {@link org.springframework.cloud.service.common.RelationalServiceInfo}
 */
public interface RelationalServiceInfoUtils {

    /**
     * Enumerated value for multiple
     */
    String MULTIPLE = "MULTIPLE";

    /**
     * Enumerated value for MySQL
     */
    String MYSQL = "MYSQL";

    /**
     * Enumerated value for none
     */
    String NONE = "NONE";

    /**
     * Enumerated value for PostgreSQL
     */
    String POSTGRESQL = "POSTGRESQL";

    /**
     * Enumerated value for an unknown type
     */
    String UNKNOWN = "UNKNOWN";

    /**
     * Returns the type of a single relational service info.  If there are none,
     * then returns {@link #NONE}.  If there is more than one, returns {@link #MULTIPLE}.  If the type is unknown,
     * returns {@link #UNKNOWN}.
     *
     * @return Returns the type of a single relational service info
     *
     * @see #MULTIPLE
     * @see #MYSQL
     * @see #NONE
     * @see #POSTGRESQL
     * @see #UNKNOWN
     */
    String relationServiceInfoType();

}
