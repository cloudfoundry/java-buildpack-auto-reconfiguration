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

import org.cloudfoundry.reconfiguration.util.CloudRelationalServiceInfoUtils;
import org.cloudfoundry.reconfiguration.util.RelationalServiceInfoUtils;
import play.Application;
import play.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A Play Framework plugin that configures JPA with the proper dialect for a single Cloud Foundry database service bound
 * to the application. This is meant to be used instead of Play Framework's built-in {@code JPAPlugin}.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public final class JPAPlugin extends play.db.jpa.JPAPlugin {

    private static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";

    private static final String POSTGRES_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";

    private final Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<String,
            EntityManagerFactory>();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Application application;

    private final Configuration configuration;

    private final RelationalServiceInfoUtils relationalServiceInfoUtils;

    /**
     * Creates a new instance
     *
     * @param application The application using this plugin
     */
    public JPAPlugin(Application application) {
        this(application, Configuration.root(), new CloudRelationalServiceInfoUtils());
    }

    JPAPlugin(Application application, Configuration configuration, RelationalServiceInfoUtils
            relationalServiceInfoUtils) {
        super(application);
        this.application = application;
        this.configuration = configuration;
        this.relationalServiceInfoUtils = relationalServiceInfoUtils;
    }

    @Override
    public EntityManager em(String key) {
        EntityManagerFactory entityManagerFactory = this.entityManagerFactories.get(key);

        if (entityManagerFactory == null) {
            return null;
        }

        return entityManagerFactory.createEntityManager();
    }

    @Override
    public boolean enabled() {
        String status = this.application.configuration().getString("cfjpaplugin");
        return "enabled".equals(status);
    }

    @Override
    public void onStart() {
        Configuration jpaConfiguration = this.configuration.getConfig("jpa");
        if (jpaConfiguration != null) {
            for (String key : jpaConfiguration.keys()) {
                String persistenceUnit = jpaConfiguration.getString(key);
                EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit,
                        getProperties());
                this.entityManagerFactories.put(key, entityManagerFactory);
            }
        }
    }

    @Override
    public void onStop() {
        for (EntityManagerFactory entityManagerFactory : this.entityManagerFactories.values()) {
            entityManagerFactory.close();
        }
    }

    Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>();

        String type = this.relationalServiceInfoUtils.relationServiceInfoType();
        if (RelationalServiceInfoUtils.NONE.equals(type)) {
            this.logger.info("No matching service found. Skipping JPA auto-reconfiguration.");
        } else if (RelationalServiceInfoUtils.MULTIPLE.equals(type)) {
            this.logger.warning("More than one matching service found. Skipping JPA auto-reconfiguration.");
        } else {
            augmentWithDialect(properties, type);
        }

        return properties;
    }

    private void augmentWithDialect(Map<String, String> properties, String type) {
        if (RelationalServiceInfoUtils.POSTGRESQL.equals(type)) {
            properties.put("hibernate.dialect", POSTGRES_DIALECT);
        } else if (RelationalServiceInfoUtils.MYSQL.equals(type)) {
            properties.put("hibernate.dialect", MYSQL_DIALECT);
        }
    }

}
