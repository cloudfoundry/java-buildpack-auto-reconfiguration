package org.cloudfoundry.runtime.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.springframework.util.FileCopyUtils;

public class CloudEnvironmentTestHelper {
	public static String getRedisServicePayload(String serviceName, 
			String hostname, int port, 
			String password, String name) {
		String payload = readTestDataFile("test-redis-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		
		return payload;
	}

	public static String getMongoServicePayload(String serviceName, 
			String hostname,
			int port, String password, String name) {
		String payload = readTestDataFile("test-mongodb-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		
		return payload;
	}

	public static String getMysqlServicePayload(String serviceName, 
			String hostname, int port, 
			String user, String password, String name) {
		String payload = readTestDataFile("test-mysql-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$user", user);
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		
		return payload;
	}
	
	public static String getPostgreSQLServicePayload(String serviceName,
			String hostname,
			int port,
			String user,
			String password,
			String name) {
		String payload = readTestDataFile("test-postgresql-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$user", user);
		payload = payload.replace("$password", password);
		payload = payload.replace("$name", name);
		
		return payload;
	}

	public static String getRabbitServicePayload(String serviceName, 
			String hostname, int port, 
			String user, String password, String name,
			String vHost) {
		String payload = readTestDataFile("test-rabbit-info.json");
		payload = payload.replace("$serviceName", serviceName);
		payload = payload.replace("$hostname", hostname);
		payload = payload.replace("$port", Integer.toString(port));
		payload = payload.replace("$user", user);
		payload = payload.replace("$pass", password);
		payload = payload.replace("$name", name);
		payload = payload.replace("$vhost", vHost);
		
		return payload;
	}

	public static String getServicesPayload(String[] mysqlServicePayloads, 
			String[] redisServicePayloads, 
			String[] mongodbServicePayloads,
			String[] rabbitServicePayloads) {
		StringBuilder payload = new StringBuilder("{");
		if (mysqlServicePayloads != null) {
			payload.append("\"mysql-5.1\":");
			payload.append(getServicePayload(mysqlServicePayloads));
		}
		if (redisServicePayloads != null) {
			payload.append("\"redis-2.2\":");
			payload.append(getServicePayload(redisServicePayloads));
		}
		if (mongodbServicePayloads != null) {
			payload.append("\"mongodb-1.8\":");
			payload.append(getServicePayload(mongodbServicePayloads));
		}
		if (rabbitServicePayloads != null) {
			payload.append("\"rabbitmq-2.4\":");
			payload.append(getServicePayload(rabbitServicePayloads));
		}
		payload.append("}");
		
		return payload.toString();
	}
	
	public static String getApplicationInstanceInfo(String uris) {
		String payload = readTestDataFile("test-application-info.json");
		payload = payload.replace("$uris", uris);
		
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
