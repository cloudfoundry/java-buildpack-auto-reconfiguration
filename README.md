# Java Buildpack Auto-Reconfiguration
This repository contains the auto-reconfiguration functionality for Spring auto-reconfiguration.

| Job | Status
| --- | ------
| `unit-test-Athens-RELEASE`       | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-RELEASE/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-RELEASE)
| `unit-test-Athens-SR1`           | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR1/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR1)
| `unit-test-Athens-SR2`           | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR2/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR2)
| `unit-test-Athens-SR3`           | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR3/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR3)
| `unit-test-Athens-SR4`           | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR4/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR4)
| `unit-test-Athens-SR5`           | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR5/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR5)
| `unit-test-Athens-SR6`           | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR6/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Athens-SR6)
| `unit-test-Brussels-RELEASE`     | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-RELEASE/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-RELEASE)
| `unit-test-Brussels-SR1`         | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR1/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR1)
| `unit-test-Brussels-SR2`         | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR2/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR2)
| `unit-test-Brussels-SR3`         | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR3/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR3)
| `unit-test-Brussels-SR4`         | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR4/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR4)
| `unit-test-Brussels-SR5`         | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR5/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR5)
| `unit-test-Brussels-SR6`         | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR6/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Brussels-SR6)
| `unit-test-Cairo-BUILD-SNAPSHOT` | [![unit-test-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Cairo-BUILD-SNAPSHOT/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/unit-test-Cairo-BUILD-SNAPSHOT)
| `deploy`                         | [![deploy-master](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/auto-reconfiguration/jobs/deploy/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/auto-reconfiguration/jobs/deploy)

## What is Auto-Reconfiguration?
Auto-reconfiguration consists of three parts.

1. it adds the `cloud` profile to Spring's list of active profiles
2. it exposes all of the properties contributed by Cloud Foundry as a `PropertySource` in the `ApplicationContext`
3. it re-writes the bean definitions of various types to connect automatically with services bound to the application.

The types that are rewritten are as follows:

| Bean Type | Service Type
| --------- | ------------
| `com.datastax.driver.core.Cluster` | Cassandra Service
| `javax.sql.DataSource` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.amqp.rabbit.connection.ConnectionFactory` | RabbitMQ Service (e.g. CloudAMQP)
| `org.springframework.data.mongodb.MongoDbFactory` | Mongo Service (e.g. MongoLab)
| `org.springframework.data.redis.connection.RedisConnectionFactory` | Redis Service (e.g. Redis Cloud)
| `org.springframework.mail.javamail.JavaMailSender` | SMTP Service

## Dependencies
Most projects will receive this project via Java Buildpack injection.  If you need to download it for diagnsotic purposes, the dependency can be defined like this:

```xml
<dependencies>
    <dependency>
        <groupId>org.cloudfoundry</groupId>
        <artifactId>java-buildpack-auto-reconfiguration</artifactId>
        <version>2.0.0.RELEASE</version>
    </dependency>
    ...
</dependencies>
```

Release artifacts can be found in the Spring release repository:

```xml
<repositories>
    <repository>
        <id>spring-releases</id>
        <name>Spring Releases</name>
        <url>https://repo.spring.io/release</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
    ...
</repositories>
```

Snapshot artifacts can be found in the Spring snapshot repository:

```xml
<repositories>
    <repository>
        <id>spring-snapshots</id>
        <name>Spring Snapshots</name>
        <url>https://repo.spring.io/snapshot</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
    ...
</repositories>
```

For Gradle, the dependencies would be defined like this:

```groovy
dependencies {
    compile 'org.cloudfoundry:java-buildpack-auto-reconfiguration:2.0.0.RELEASE'
    ...
}
```

Snapshot artifacts can be found in the Spring release repository:

```groovy
repositories {
    maven { url 'http://repo.spring.io/release' }
    ...
}
```

Snapshot artifacts can be found in the Spring snapshot repository:

```groovy
repositories {
    maven { url 'http://repo.spring.io/snapshot' }
    ...
}
```

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
This buildpack is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
[contributor guidelines]: CONTRIBUTING.md
[Pull requests]: http://help.github.com/send-pull-requests
