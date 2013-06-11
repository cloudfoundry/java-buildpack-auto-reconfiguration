package org.cloudfoundry.runtime.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.springframework.util.FileCopyUtils;

public class CloudEnvironmentTestHelper {
	public static String getRedisServicePayload(String version, String serviceName,
			String hostname, int port,
			String password, String name) {
		String payload = readTestDataFile("test-redis-info.json");
		payload = payload.replace("$version", version);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		
		return payload;
	}

	public static String getRedisCloudServicePayload(String serviceName,
			String hostname, int port, String password) {
		String payload = readTestDataFile("test-rediscloud-info.json");
		payload = payload.replace("$version", "n/a");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$password", password);

		return payload;
	}


	public static String getMongoServicePayload(String version, String serviceName,
			String hostname, int port,
			String username, String password, String db, String name) {
		String payload = readTestDataFile("test-mongodb-info.json");
		payload = payload.replace("$version", version);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$username", username);
		payload = payload.replace("$password", password);
		payload = payload.replace("$db", db);
		payload = payload.replace("$name", name);
		
		return payload;
	}

	public static String getMongoLabServicePayload(String serviceName,
			String hostname, int port,
			String username, String password, String db) {
		String payload = readTestDataFile("test-mongolab-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$username", username);
		payload = payload.replace("$password", password);
		payload = payload.replace("$db", db);

		return payload;
	}

	private static String getRelationalServicePayload(String templateFileName, String version, String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		String payload = readTestDataFile(templateFileName);
		payload = payload.replace("$version", version);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$user", user);
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		
		return payload;
	}
	
	public static String getMysqlServicePayload(String version, String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalServicePayload("test-mysql-info.json", version, serviceName, hostname, port, user, password, name);
	}

	public static String getPostgreSQLServicePayload(String version, String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalServicePayload("test-postgresql-info.json", version, serviceName, hostname, port, user, password, name);
	}

	public static String getElephantSQLServicePayload(String version, String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalServicePayload("test-elephantsql-info.json", version, serviceName, hostname, port, user, password, name);
	}

	public static String getRdsServicePayload(String version, String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalServicePayload("test-rds-info.json", version, serviceName, hostname, port, user, password, name);
	}

	public static String getCleardbServicePayload(String version, String serviceName,
			String hostname, int port,
			String user, String password, String name) {
		return getRelationalServicePayload("test-cleardb-info.json", version, serviceName, hostname, port, user, password, name);
	}


	public static String getRabbitServicePayload(String version, String serviceName,
			String hostname, int port,
			String user, String password, String name,
			String vHost) {
		String payload = readTestDataFile("test-rabbit-info.json");
		payload = payload.replace("$version", version);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$user", user);
		payload = payload.replace("$pass", password);
		payload = payload.replace("$name", name);
		payload = payload.replace("$vhost", vHost);
		
		return payload;
	}

	public static String getRabbitSRSServicePayload(String version, String serviceName, String url) {
		String payload = readTestDataFile("test-rabbit-srs-info.json");
		payload = payload.replace("$version", version);
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$url", url);

		return payload;
	}

	public static String getCloudAmqpServicePayload(String serviceName, String uri) {
		String payload = readTestDataFile("test-cloudamqp-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$uri", uri);

		return payload;
	}

	public static String getFullServicesPayload() {
		return getServicesPayload(new String[]{getMysqlServicePayload("1.1", "mysql-1", "mysql-host", 1111, "mysql-user", "mysql-pass", "database-123")},
				new String[]{getRedisServicePayload("2.2", "redis-2", "redis-host", 2222, "redis-pass", "r1")},
				new String[]{getMongoServicePayload("3.3", "mongo-3", "mongo-host", 3333, "mongo-user", "mongo-pass", "db", "name")},
				new String[]{getRabbitServicePayload("4.4", "rabbit-4", "rabbit-host", 4444, "rabbit-user", "rabbit-pass", "r1", "vhost")});
	}

	public static String getServicesPayload(String[] mysqlServicePayloads,
			String[] redisServicePayloads, 
			String[] mongodbServicePayloads,
			String[] rabbitServicePayloads) {
		StringBuilder payload = new StringBuilder("{");
		if (mysqlServicePayloads != null) {
			if (payload.length() > 1) {
				payload.append(",");
			}
			payload.append("\"mysql-5.1\":");
			payload.append(getServicePayload(mysqlServicePayloads));
		}
		if (redisServicePayloads != null) {
			if (payload.length() > 1) {
				payload.append(",");
			}
			payload.append("\"redis-2.2\":");
			payload.append(getServicePayload(redisServicePayloads));
		}
		if (mongodbServicePayloads != null) {
			if (payload.length() > 1) {
				payload.append(",");
			}
			payload.append("\"mongodb-1.8\":");
			payload.append(getServicePayload(mongodbServicePayloads));
		}
		if (rabbitServicePayloads != null) {
			if (payload.length() > 1) {
				payload.append(",");
			}
			payload.append("\"rabbitmq-2.4\":");
			payload.append(getServicePayload(rabbitServicePayloads));
		}
		payload.append("}");
		
		return payload.toString();
	}
	
	public static String getApplicationInstanceInfo(String name, String... uri) {
		String payload = readTestDataFile("test-application-info.json");
		payload = payload.replace("$name", name);
		StringBuilder uris = new StringBuilder();
		for (String u : uri) {
			if (uris.length() > 0) {
				uris.append(",");
			}
			uris.append("\"");
			uris.append(u);
			uris.append("\"");
		}
		payload = payload.replace("$uris", uris.toString());
		
		return payload;
	}
	
	private static String getServicePayload(String[] servicePayloads) {
		StringBuilder payload = new StringBuilder("[");
		
		// In Scala, this would have been servicePayloads mkString "," :-)
		for (int i = 0; i < servicePayloads.length; i++) {
			payload.append(servicePayloads[i]);
			if (i != servicePayloads.length - 1) {
				payload.append(",");
			}
		}
		payload.append("]");
		
		return payload.toString();
	}
	
	private static String readTestDataFile(String fileName) {
		try {
			Reader fileReader = new InputStreamReader(CloudEnvironmentTestHelper.class.getResourceAsStream(fileName));
			StringWriter writer = new StringWriter();
			FileCopyUtils.copy(fileReader, writer);
			return writer.toString();
		} catch (IOException e) {
			throw new IllegalArgumentException("Could read " + fileName);
		}
	}
	
}
