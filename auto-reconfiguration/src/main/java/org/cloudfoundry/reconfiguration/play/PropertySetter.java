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

package org.cloudfoundry.reconfiguration.play;

import java.util.Set;

interface PropertySetter {

    /**
     * The class name for the MySQL/MariaDB driver
     */
    String MYSQL_DRIVER_CLASS = "org.mariadb.jdbc.Driver";

    /**
     * The class name for the PostgreSQL driver
     */
    String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

    /**
     * Set system properties for each cloud property, such that a Play Framework user can refer to these properties in a
     * conf file. For example: {@code db.default.url=${cloud.services.mysql.connection.url}}
     */
    void setCloudProperties();

    /**
     * Sets a system property to apply evolutions to the specified Play Framework databases
     *
     * @param names The names of the databases in the Play Framework configuration
     */
    void setDatabaseProperties(Set<String> names);

}
