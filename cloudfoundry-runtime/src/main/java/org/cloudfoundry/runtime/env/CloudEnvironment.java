package org.cloudfoundry.runtime.env;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Simpler access to Cloud Foundry environment.
 * <p>
 * This class interprets environment variables and provide a simple
 * access without needing JSON parsing.
 * </p>
 * 
 * @author Ramnivas Laddad
 * @author Scott Andrews
 * @author Thomas Risberg
 *
 */
public class CloudEnvironment {

    private ObjectMapper objectMapper = new ObjectMapper();
    private EnvironmentAccessor environment = new EnvironmentAccessor();
    private static Map<Class<? extends AbstractServiceInfo>, Set<String>> serviceTypeToLabels = new HashMap<Class<? extends AbstractServiceInfo>, Set<String>>();

    private static void labelledServiceType(Class<? extends AbstractServiceInfo> serviceType,
            String label) {
        Set<String> labels = serviceTypeToLabels.get(serviceType);
        if (labels == null) {
            labels = new HashSet<String>();
            serviceTypeToLabels.put(serviceType, labels);
        }

        labels.add(label);
    }

    static {
        labelledServiceType(RdbmsServiceInfo.class, "mysql-5.1");
        labelledServiceType(RdbmsServiceInfo.class, "postgresql-9.0");
        labelledServiceType(RdbmsServiceInfo.class, "sqlfire-1.0");
        labelledServiceType(RedisServiceInfo.class, "redis-2.2");
        labelledServiceType(MongoServiceInfo.class, "mongodb-1.8");
        labelledServiceType(RabbitServiceInfo.class, "rabbitmq-2.4");
    }

    /* package for testing purpose */
    void setCloudEnvironment(EnvironmentAccessor environment) {
        this.environment = environment;
    }

    public String getValue(String key) {
        return environment.getValue(key);
    }

    public boolean isCloudFoundry() {
        return getValue("VCAP_APPLICATION") != null;
    }

    @SuppressWarnings("unchecked")
    public ApplicationInstanceInfo getInstanceInfo() {
        String instanceInfoString = getValue("VCAP_APPLICATION");
        if (instanceInfoString == null || instanceInfoString.trim().isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> infoMap = objectMapper.readValue(instanceInfoString, Map.class);
            return new ApplicationInstanceInfo(infoMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getCloudApiUri() {
        ApplicationInstanceInfo instanceInfo = getInstanceInfo();
        if (instanceInfo == null) {
            throw new IllegalArgumentException("There is no cloud API urls in a non-cloud deployment");
        }
        List<String> uris = instanceInfo.getUris();
        String defaultUri = uris.get(0);
        return "api" + defaultUri.substring(defaultUri.indexOf("."));
    }

    /**
     * Return object representation of the VCAP_SERIVCES environment variable
     * <p>
     * Returns a map whose key is the label (for example "redis-2.2") of the
     * service and value is a list of services for that label. Each list element
     * is a map with service attributes.
     * </p>
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<Map<String, Object>>> getRawServices() {
        String servicesString = getValue("VCAP_SERVICES");
        if (servicesString == null || servicesString.length() == 0) {
            return new HashMap<String, List<Map<String, Object>>>();
        }
        try {
            return objectMapper.readValue(servicesString, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> getServices() {
        Map<String, List<Map<String, Object>>> rawServices = getRawServices();

        List<Map<String, Object>> flatServices = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : rawServices.entrySet()) {
            flatServices.addAll(entry.getValue());
        }
        return flatServices;
    }

    private Map<String, Object> getServiceDataByName(String name) {
        List<Map<String, Object>> services = getServices();

        for (Map<String, Object> service : services) {
            if (service.get("name").equals(name)) {
                return service;
            }
        }
        return null;
    }

    private List<Map<String, Object>> getServiceDataByLabels(Set<String> labels) {
        List<Map<String, Object>> services = getServices();
        List<Map<String, Object>> matchedServices = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> service : services) {
            if (labels.contains(service.get("label"))) {
                matchedServices.add(service);
            }
        }

        return matchedServices;
    }

    public <T extends AbstractServiceInfo> T getServiceInfo(String name, Class<T> serviceInfoType) {
        Map<String, Object> serviceInfoMap = getServiceDataByName(name);
        Set<String> labels = serviceTypeToLabels.get(serviceInfoType);

        if (labels != null && labels.contains(serviceInfoMap.get("label"))) {
            return getServiceInfo(serviceInfoMap, serviceInfoType);
        } else {
            return null;
        }
    }

    public <T extends AbstractServiceInfo> List<T> getServiceInfos(Class<T> serviceInfoType) {
        Set<String> labels = serviceTypeToLabels.get(serviceInfoType);
        List<T> serviceInfos = new ArrayList<T>();

        if (labels != null) {
            List<Map<String, Object>> serviceInfoMaps = getServiceDataByLabels(labels);

            for (Map<String, Object> serviceInfoMap : serviceInfoMaps) {
                serviceInfos.add(getServiceInfo(serviceInfoMap, serviceInfoType));
            }
        }

        return serviceInfos;
    }

    private <T extends AbstractServiceInfo> T getServiceInfo(Map<String, Object> serviceInfoMap, Class<T> serviceInfoType) {
        try {
            Constructor<T> ctor = serviceInfoType.getConstructor(Map.class);
            return ctor.newInstance(serviceInfoMap);
        } catch (Exception e) {
            throw new CloudServiceException("Failed to create service information for " + serviceInfoMap.get("name"), e);
        }
    }

    /**
     * <p>General properties take the form:
     * <code><pre>
     * cloud.application.name = helloworld
     * cloud.provider.url = cloudfoundry.com
     * </pre></code>
     *
     * <p>Service specific properties are also exposed for each bound service:
     * <code><pre>
     * cloud.services.customerDb.type = mysql-5.1
     * cloud.services.customerDb.plan = free
     * cloud.services.customerDb.connection.hostname = ...
     * cloud.services.customerDb.connection.port = ...
     * etc...
     * </pre></code>
     *
     * <p>If a there is only a single service of a given type, that service is
     * aliased to the service type.  For example, if there is only a single MySQL
     * service bound to the application, the service properties will also be
     * exposed under the '<code>mysql</code>' key:
     * <code><pre>
     * cloud.services.mysql.type = mysql-5.1
     * cloud.services.mysql.plan = free
     * cloud.services.mysql.connection.hostname = ...
     * cloud.services.mysql.connection.port = ...
     * etc...
     * </pre></code>
     * @return
     */
    public Properties getCloudProperties() {
        Properties properties = new Properties();
        properties.putAll(providerProperties());
        properties.putAll(applicationProperties());
        properties.putAll(serviceProperties());
        return properties;
    }

    private Properties providerProperties() {
        Properties properties = new Properties();
        properties.put("cloud.provider.url", getCloudApiUri().split("\\.", 2)[1]);
        return properties;
    }

    private Properties applicationProperties() {
        Properties properties = new Properties();
        properties.put("cloud.application.name", getInstanceInfo().getName());
        return properties;
    }

    private Properties serviceProperties() {
        Properties properties = new Properties();
        Map<String, Integer> serviceCounts = new HashMap<String, Integer>();
        List<Map<String, Object>> services = getServices();
        for (Map<String, Object> service : services) {
            String shortType = serviceShortType(service);
            // index services properties by name
            properties.putAll(servicePropertiesHelper("cloud.services." + service.get("name"), service));
            // count services by type (needed in next iteration)
            int count = serviceCounts.containsKey(shortType) ? serviceCounts.get(shortType) : 0;
            serviceCounts.put(shortType, count + 1);
        }
        for (Map<String, Object> service : services) {
            // alias service properties by type, if unique and available
            String shortType = serviceShortType(service);
            if (serviceCounts.get(shortType) == 1 && !properties.containsKey("cloud.services." + shortType + ".type")) {
                properties.putAll(servicePropertiesHelper("cloud.services." + shortType, service));
            }
        }
        return properties;
    }

    @SuppressWarnings("unchecked")
    private Properties servicePropertiesHelper(String propertyBase, Map<String, Object> service) {
        Properties source = new Properties();
        source.put(propertyBase + ".plan", service.get("plan").toString());
        source.put(propertyBase + ".type", service.get("label").toString());
        for (Entry<String, Object> connectionProperty : ((Map<String, Object>) service.get("credentials")).entrySet()) {
            source.put(propertyBase + ".connection." + connectionProperty.getKey(), connectionProperty.getValue().toString());
        }
        return source;
    }

    private String serviceShortType(Map<String, Object> service) {
        String type = (String) service.get("label");
        return type.split("-", 2)[0];
    }

    /**
     * Environment available to the deployed app.
     *
     * The main purpose of this class is to allow unit-testing of {@link CloudEnvironment}
     *
     */
    public static class EnvironmentAccessor {

        public String getValue(String key) {
            return System.getenv(key);
        }
    }
}
