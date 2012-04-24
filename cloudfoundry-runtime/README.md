## Overview

cloudfoundry-runtime is a Java library for interacting with Cloud Foundry services.  It provides methods for obtaining pre-configured clients and connection properties.  It also provides a namespace for configuring service connections in a Spring application.

## Using cloudfoundry-runtime

Include the org.cloudfoundry:cloudfoundry-runtime dependency in your build. If you are using Maven add this dependency to your pom.xml:

``` bash
<dependency>
     <groupId>org.cloudfoundry</groupId>
     <artifactId>cloudfoundry-runtime</artifactId>
     <version>0.8.1</version>
</dependency>
```

You will also need to have the Spring Framework Milestone Repository in your pom.xml. Simply add this repository declaration to your repositories:

```bash
<repository>
      <id>org.springframework.maven.milestone</id>
      <name>Spring Framework Maven Milestone Repository</name>
      <url>http://maven.springframework.org/milestone</url>
  </repository>
```

## Getting environment information using Java API
The [Java API](http://cf-runtime-api.cloudfoundry.com) includes a class called CloudEnvironment, which provides a Java object model from the JSON in the VCAP_SERVICES environment variable.  This format makes it easier to provide service connection info to your code that creates clients.

Here is an example that retrieves information about a Mongo service named "my-mongo"

```java
Cloud Environment environment= new CloudEnvironment();
MongoServiceInfo service = environment.getServiceInfo("my-mongo",MongoServiceInfo.class);
System.out.println("Mongo available at host: " + service.getHost() + " and port: " + service.getPort());
```

## Connecting to services using Java API
If you don't care to use the cloud namespace, you can still take advantage of the [Java API](http://cf-runtime-api.cloudfoundry.com) to make programmatic service connections.  See the javadoc for all extensions of AbstractServiceCreator.  The service creators for Mongo and Redis require Spring Data.  The RabbitServiceCreator requires Spring AMQP.  Service creators for MySQL and PostgreSQL will create a Commons DBCP DataSource if the libraries are present, else they will attempt to create a Tomcat DataSource.

Here is an example that creates a connection to a Mongo service named "my-mongo":
```java
CloudEnvironment environment = new CloudEnvironment();
MongoDbFactory mongo = new MongoServiceCreator().createService(environment.getService("my-mongo",MongoServiceInfo.class))
```

## Using the \<cloud\> namespace in Spring applications
To add the \<cloud\> namespace to your application context files, you need to declare the namespace and also provide the schema location. Once this is declared, you can use the \<cloud\> namespace elements. Here is the complete example:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:context="http://www.springframework.org/schema/context"
  xmlns:cloud="http://schema.cloudfoundry.org/spring"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd
    http://schema.cloudfoundry.org/spring http://schema.cloudfoundry.org/spring/cloudfoundry-spring-0.8.xsd">

    <cloud:mongo-db-factory id="mongoDbFactory">

    <bean id="mongoTemplate" class="org.springframework.data.mongodb.core.MongoTemplate">
        <constructor-arg ref="mongoDbFactory"/>
    </bean>

    <!-- more beans for your app -->

</beans>
```

### Available namespace elements

#### \<cloud:data-source\>
The \<cloud:data-source\> element provides a convenient way to configure the JDBC DataSource for your Spring application.

##### Basic attributes
- id – defaults to service name
- service-name – only needed if you have multiple relational database services bound to the app

A simple example DataSource configuration to be injected into a JdbcTemplate would look like this, with the only attribute used specifying the id of the datasource bean.
```xml
<cloud:data-source id="dataSource" />

<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
  <property name="dataSource" ref="dataSource" />
</bean>
```

There are sub-elements that can be used to configure specific connections and pool settings. The \<cloud:data-source\> namespace element supports the most commonly used configuration options via the \<cloud:connection\> and \<cloud:pool\> sub-elements. The supported options are explained below:

##### \<cloud:connection\> options

<html>
<table>
<tr>
<th>Namespace attribute</th>
<th>Description</th>
<th>Type</th>
</tr>
<tr>
<td>properties</td>
<td>The connection properties that will be sent to the JDBC driver when establishing new connections. Format of the string must be "propertyName=property;"</td>
<td>string</td>
</tr>
</table>
</html>

##### \<cloud:pool\> options

<html>
<table>
<tr>
<th>Namespace attribute</th>
<th>Description</th>
<th>Type</th>
<th>Default</th>
</tr>
<tr>
<td>pool-size</td>
<td>Either the maximum number of connections in the pool or a range specifying minimum and maximum size separated by a dash.</td>
<td>int</td>
<td>Uses the default settings of the Apache Commons Pool which are 0 for min and 8 for max size</td>
</tr>
<tr>
<td>max-wait-time</td>
<td>The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception. -1 indicates unlimited wait.</td>
<td>int</td>
<td>Uses the default setting of the Apache Commons Pool which is unlimited (-1)</td>
</tr>
</table>
</html>

#### \<cloud:mongo-db-factory\>
The \<cloud:mongo-db-factory\> element provides a convenient way to configure the MongoDB connection factory for your Spring application.

##### Basic attributes
- id – defaults to service name
- service-name – only needed if you have multiple MongoDB services bound to the app
- write-concern – the WriteConcern to use for all DB connections created (NONE, NORMAL, SAFE, FSYNC_SAFE). If this is not specified then no WriteConcern will be set for the DB connections and all writes will default to NORMAL

The values for the write-concern attribute correspond to the values available in the com.mongodb.WriteConcern class.

<html>
<table>
<tr>
<th>Value</th>
<th>Description</th>
</tr>
<tr>
<td>NONE</td>
<td>No exceptions are raised, even for network issues</td>
</tr>
<tr>
<td>NORMAL</td>
<td>Exceptions are raised for network issues, but not server errors</td>
</tr>
<tr>
<td>SAFE</td>
<td>Exceptions are raised for network issues, and server errors; waits on a server for the write operation</td>
</tr>
<tr>
<td>FSYNC_SAFE</td>
<td>Exceptions are raised for network issues, and server errors and the write operation waits for the server to flush the data to disk</td>
</tr>
</table>
</html>

A simple example MongoDbFactory configuration to be injected into a MongoTemplate would look like this, with the only attribute used specifying the id of the mongoDbFactory bean.
```xml
<cloud:mongo-db-factory id="mongoDbFactory" />

<bean id="mongoTemplate" class="org.springframework.data.mongodb.core.MongoTemplate">
    <constructor-arg ref="mongoDbFactory"/>
</bean>
```

There are some advanced configuration attributes that are available using the \<cloud:mongo-options\> sub-element and they are listed below.

##### \<cloud:mongo-options\>

<html>
<table>
<tr>
<th>Namespace attribute</th>
<th>Description</th>
<th>Type</th>
<th>Default</th>
</tr>
<tr>
<td>connections-per-host</td>
<td>The maximum number of connections allowed per host for the Mongo instance. Those connections will be kept in a pool when idle. Once the pool is exhausted, any operation requiring a connection will block waiting for an available connection.</td>
<td>int</td>
<td>The Mongo driver has a default of 10</td>
</tr>
<tr>
<td>max-wait-time</td>
<td>The maximum wait time in ms that a thread may wait for a connection to become available.</td>
<td>int</td>
<td>The Mongo driver has a default of 120000</td>
</tr>
</table>
</html>

#### \<cloud:redis-connection-factory\>
The \<cloud:redis-connection-factory\> element provides a convenient way to configure the Redis connection factory for your Spring application.

##### Basic attributes
- id – defaults to service name
- service-name – only needed if you have multiple Redis services bound to the app

A simple example RedisConnectionFactory configuration to be injected into a RedisTemplate would look like this, with the only attribute used specifying the id of the redisConnectionFactory bean.
```xml
<cloud:redis-connection-factory id="redisConnectionFactory" />

<bean id="redisTemplate" class="org.springframework.data.redis.core.StringRedisTemplate">
    <property name="connection-factory" ref="redisConnectionFactory"/>
</bean>
```
The advanced configuration attributes that are available via the \<cloud:pool\> sub-element are listed below.

##### \<cloud:pool\> options

<html>
<table>
<tr>
<th>Namespace attribute</th>
<th>Description</th>
<th>Type</th>
<th>Default</th>
</tr>
<tr>
<td>pool-size</td>
<td>Either the maximum number of connections in the pool or a range specifying minimum and maximum size separated by a dash.</td>
<td>int</td>
<td>Uses the default settings of the Apache Commons Pool which are 0 for min and 8 for max size</td>
</tr>
<tr>
<td>max-wait-time</td>
<td>The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception.</td>
<td>int</td>
<td>Uses the default setting of the Apache Commons Pool which is unlimited (-1)</td>
</tr>
</table>
</html>

#### \<cloud:rabbit-connection-factory\>
The \<cloud:rabbit-connection-factory\> element provides a convenient way to configure the RabbitMQ connection factory for your Spring application.

##### Basic Attributes
- id – defaults to service name
- service-name – only needed if you have multiple RabbitMQ services bound to the app

A simple example RabbitConnectionFactory configuration to be injected into a RabbitTemplate would look like this, with the only attribute used specifying the id of the rabbitConnectionFactory bean. This example uses the <rabbit> namespace in addition to the <cloud> one.
```xml
<cloud:rabbit-connection-factory id="rabbitConnectionFactory" />

<rabbit:template id="rabbitTemplate"
    connection-factory="rabbitConnectionFactory" />
```
The advanced configuration attributes that are available via the \<cloud:rabbit-options\> are listed below.

##### \<cloud:rabbit-options\>

<html>
<table>
<tr>
<th>Namespace attribute</th>
<th>Description</th>
<th>Type</th>
<th>Default</th>
</tr>
<tr>
<td>channel-cache-size</td>
<td>The size of the channel cache.</td>
<td>int</td>
<td>The default is 1</td>
</tr>
</table>
</html>

#### \<cloud:service-scan\>
The \<cloud:service-scan\> element scans all services bound to the application and creates a bean of an appropriate type for each. You can think of this element as a cloud extension of \<context:component-scan\> in core Spring, which scans the classpath for beans with certain annotations and creates a bean for each. The \<cloud:service-scan\> is especially useful during the initial phases of application development, where you want immediate access to service beans without adding a \<cloud\> element for each new service bound.
Once you include a \<cloud:service-scan\> element in application context, then in your Java code, simply add @Autowired dependencies for each bound service:
```java
@Autowired DataSource dataSource;
@Autowired ConnectionFactory rabbitConnectionFactory;
@Autowired RedisConnectionFactory redisConnectionFactory;
@Autowired MongoDbFactory mongoDbFactory;
```
Voila! You have access to all services without breaking a sweat.
The above style works as long as you have only one service of each type bound to the application. In other cases, you need to use
the @Qualifier to specify the service name (each automatically created bean is named after the associated service name).
```java
@Autowired @Qualifier("inventory-db") DataSource inventoryDataSource;
@Autowired @Qualifier("pricing-db") DataSource pricingDataSource;
```
Here, the inventoryDataSource bean will be bound to the inventory-db service and the pricingDataSource bean will be bound to the pricing-db service.

#### \<cloud:properties\>

##### Basic attributes

- id – the name of the Properties bean

The \<cloud:properties\> element exposes basic information about services that can be consumed with Spring’s property placeholder support. The properties exposed match those automatically enabled for a Spring 3.1 application.