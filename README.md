# Java Buildpack Auto-Reconfiguration
This repository contains the auto-reconfiguration functionality for Spring and Play Framework auto-reconfiguration.  It also contains a replacement Play Framework JPA Plugin that can be properly auto-reconfigured.

## What is Auto-Reconfiguration?
Auto-reconfiguration consists of three parts.

1. it adds the `cloud` profile to Spring's list of active profiles
2. it exposes all of the properties contributed by Cloud Foundry as a `PropertySource` in the `ApplicationContext`
3. it re-writes the bean definitions of various types to connect automatically with services bound to the application.

The types that are rewritten are as follows:

| Bean Type | Service Type
| --------- | ------------
| `javax.sql.DataSource` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.amqp.rabbit.connection.ConnectionFactory` | RabbitMQ Service (e.g. CloudAMQP)
| `org.springframework.data.mongodb.MongoDbFactory` | Mongo Service (e.g. MongoLab)
| `org.springframework.data.redis.connection.RedisConnectionFactory` | Redis Service (e.g. Redis Cloud)
| `org.springframework.orm.hibernate3.AbstractSessionFactoryBean` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.orm.hibernate4.LocalSessionFactoryBean` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.orm.jpa.AbstractEntityManagerFactoryBean` | Relational Data Services (e.g. ClearDB, ElephantSQL)

More information about Java application development on Cloud Foundry can be found here:

* [Cloud Foundry Services for Spring](https://docs.cloudfoundry.org/buildpacks/java/gsg-spring.html)
* [Cloud Foundry Services for Grails](https://docs.cloudfoundry.org/buildpacks/java/grails-service-bindings.html)
* [Cloud Foundry Services for Play](https://docs.cloudfoundry.org/buildpacks/java/play-service-bindings.html)

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
This buildpack is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
[contributor guidelines]: CONTRIBUTING.md
[Pull requests]: http://help.github.com/send-pull-requests
[`spring-cloud`]: https://github.com/spring-projects/spring-cloud
