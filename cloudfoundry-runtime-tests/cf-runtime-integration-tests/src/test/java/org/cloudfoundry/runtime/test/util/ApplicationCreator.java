/*
 * Copyright 2009-2011 the original author or authors.
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
package org.cloudfoundry.runtime.test.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.cloudfoundry.client.lib.CloudApplication;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudService;
import org.cloudfoundry.client.lib.ServiceConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Responsible for creating Cloud Foundry apps and services to use for
 * integration testing
 *
 * @author Jennifer Hickey
 *
 */
public class ApplicationCreator {

	private CloudFoundryClient client;

	private RestTemplate restTemplate = new RestTemplate();

	/**
	 *
	 * @param client
	 *            The Clound Foundry connection to use
	 */
	public ApplicationCreator(CloudFoundryClient client) {
		this.client = client;
	}

	/**
	 *
	 * @param warFile
	 *            The path to the WAR to deploy
	 * @param appName
	 *            The application name
	 * @param serviceNames
	 *            Names of services to bind to the app
	 * @return The created {@link CloudApplication}
	 * @throws IOException
	 */
	public CloudApplication createAndUploadTestApp(File warFile, String appName, List<String> serviceNames,
			List<String> uris) throws IOException {
		client.createApplication(appName, CloudApplication.SPRING,
				client.getDefaultApplicationMemory(CloudApplication.SPRING), uris, serviceNames);
		client.uploadApplication(appName, warFile.getCanonicalPath());
		return client.getApplication(appName);
	}

	/**
	 * Creates a data service (key-value or database)
	 *
	 * @param serviceName
	 *            The name of the service
	 * @param vendor
	 *            The vendor (for example, "mongodb", "mysql", "postgresql", "rabbitmq", or "redis")
	 * @return The created {@link CloudService}
	 */
	public CloudService createService(String serviceName, String vendor) {
		ServiceConfiguration dataServiceConfiguration = getServiceConfiguration(vendor);
		CloudService service = new CloudService();
		service.setTier("free");
		service.setType(dataServiceConfiguration.getType());
		service.setVersion(dataServiceConfiguration.getVersion());
		service.setName(serviceName);
		service.setVendor(dataServiceConfiguration.getVendor());

		client.createService(service);
		return service;
	}

	private ServiceConfiguration getServiceConfiguration(String vendor) {
		List<ServiceConfiguration> serviceConfigurations = client.getServiceConfigurations();
		for (ServiceConfiguration sc : serviceConfigurations) {
			if (sc.getVendor().equals(vendor)) {
				return sc;
			}
		}
		return null;
	}

	/**
	 * Periodically performs a GET against the specified URL via RestTemplate to
	 * check for app availability.
	 *
	 * @param urlToCheck
	 *            A RESTful URL belonging to the app we are status-ing
	 * @param retryInterval
	 *            The interval in milliseconds to wait between pinging the URL
	 * @param timeout
	 *            The timeout for checking the URL
	 * @return false if the GET is not successful within the default timeout (2
	 *         minutes)
	 */
	public boolean isAppAvailable(final String urlToCheck, final long retryInterval, final long timeout) {
		try {
			Thread.sleep(60000l);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SpinBarrier waitForAppStart = new SpinBarrier(timeout, retryInterval,new SpinBarrierCondition() {
			@Override
			public boolean evaluate() {
				try {
					restTemplate.getForObject(urlToCheck, String.class);
					return true;
				} catch (HttpClientErrorException e) {
					if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
						return false;
					}
					throw e;
				}
			}
		});
		return waitForAppStart.waitFor();
	}
}
