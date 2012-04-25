package org.cloudfoundry.reconfiguration.spring.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml")
public class CloudAppAnnotationConfigAutoReconfig {
}
