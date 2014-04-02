# Java Buildpack Auto-Reconfiguration
[![Build Status](https://travis-ci.org/cloudfoundry/java-buildpack-auto-reconfiguration.svg?branch=master)](https://travis-ci.org/cloudfoundry/java-buildpack-auto-reconfiguration)

This repository contains the auto-reconfiguration functionality for Spring and Play Framework auto-reconfiguration.  It also contains a replacement Play Framework JPA Plugin that can be properly auto-reconfigured.

## What is Auto-Reconfiguration?
Auto-reconfiguration consists of three parts.  First, it adds the `cloud` profile to Spring's list of active profiles.  Second it exposes all of the properties contributed by Cloud Foundry as a `PropertySource` in the `ApplicationContext`.  Finaly it re-writes the bean defintitions of various types to connect automatically with services bound to the application.  The types that are rewritten are as follows:

| Bean Type | Service Type
| --------- | ------------
| `javax.sql.DataSource` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.amqp.rabbit.connection.ConnectionFactory` | RabbitMQ Service (e.g. CloudAMQP)
| `org.springframework.data.mongodb.MongoDbFactory` | Mongo Service (e.g. MongoLab)
| `org.springframework.data.redis.connection.RedisConnectionFactory` | Redis Service (e.g. Redis Cloud)
| `org.springframework.orm.hibernate3.AbstractSessionFactoryBean` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.orm.hibernate4.LocalSessionFactoryBean` | Relational Data Services (e.g. ClearDB, ElephantSQL)
| `org.springframework.orm.jpa.AbstractEntityManagerFactoryBean` | Relational Data Services (e.g. ClearDB, ElephantSQL)

More information about Spring on Cloud Foundry can be found here:

* [Cloud Foundry Services With Spring, Part 1 : The Basics](http://spring.io/blog/2011/10/13/using-cloud-foundry-services-with-spring-part-1-the-basics)
* [Cloud Foundry Services With Spring, Part 2 : Autoreconfiguration](http://spring.io/blog/2011/11/04/using-cloud-foundry-services-with-spring-part-2-auto-reconfiguration)
* [Cloud Foundry Services With Spring, Part 3 : The Cloud Namespace](http://spring.io/blog/2011/11/09/using-cloud-foundry-services-with-spring-part-3-the-cloud-namespace)
* [Cloud Foundry Services With Spring, Part 4 : Spring Profiles](http://spring.io/blog/2011/11/10/using-cloud-foundry-services-with-spring-part-4-spring-profiles)

## `cloudfoundry-runtime`
This repository was previously home to the `cloudfoundry-runtime` project.  That project has been discontinued in favor of the [`spring-cloud`][] project.  The last commit in this repository that includes the `cloudfoundry-runtime` code has been tagged as [`cloudfoundry-runtime`][].

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
This buildpack is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
[`cloudfoundry-runtime`]: https://github.com/cloudfoundry/java-buildpack-auto-reconfiguration/tree/cloudfoundry-runtime
[contributor guidelines]: CONTRIBUTING.md
[Pull requests]: http://help.github.com/send-pull-requests
[`spring-cloud`]: https://github.com/spring-projects/spring-cloud
