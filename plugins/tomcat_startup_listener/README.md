# Tomcat Startup Listener

Detects when Tomcat starts and creates a tomcat.state file with the contents:

    { "state": "RUNNING" }


## Build instructions

    $ mvn/package

