package org.cloudfoundry.reconfiguration.play;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;

import play.Application;
import play.Configuration;

/**
 * A Plugin that configures JPA with the proper dialect for a single Cloud
 * Foundry database service bound to the application. This is meant to be used
 * instead of Play's built-in JPAPlugin.
 *
 * @author Jennifer Hickey
 */
public class JPAPlugin extends play.db.jpa.JPAPlugin {

	static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";

	static final String POSTGRES_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";

	private final Map<String, EntityManagerFactory> emfs = new HashMap<String, EntityManagerFactory>();

	private AppConfiguration appConfiguration;

	private Application application;

	public JPAPlugin(Application application) {
		this(application, new AppConfiguration(new CloudEnvironment()));
	}

	public JPAPlugin(Application application, AppConfiguration appConfiguration) {
		super(application);
		this.application = application;
		this.appConfiguration = appConfiguration;
	}

	/**
	 * Reads the configuration file and initialises required JPA
	 * EntityManagerFactories.
	 */
	public void onStart() {
		Configuration jpaConf = Configuration.root().getConfig("jpa");
		if (jpaConf != null) {
			for (String key : jpaConf.keys()) {
				String persistenceUnit = jpaConf.getString(key);
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit,
						getProperties());
				emfs.put(key, emf);
			}
		}
	}

	Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		RdbmsServiceInfo dbservice = appConfiguration.getDatabaseBinding();
		if (dbservice != null) {
			if (dbservice.getLabel().startsWith("postgres")) {
				properties.put("hibernate.dialect", POSTGRES_DIALECT);
			} else if (dbservice.getLabel().startsWith("mysql")) {
				properties.put("hibernate.dialect", MYSQL_DIALECT);
			}
		}
		return properties;
	}

	@Override
	public boolean enabled() {
		// Only enable this plugin if explicitly enabled
		String status = application.configuration().getString("cfjpaplugin");
		return "enabled".equals(status);
	}

	public void onStop() {
		for (EntityManagerFactory emf : emfs.values()) {
			emf.close();
		}
	}

	public EntityManager em(String key) {
		EntityManagerFactory emf = emfs.get(key);
		if (emf == null) {
			return null;
		}
		return emf.createEntityManager();
	}

}