package org.cloudfoundry.reconfiguration.spring.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Class with @Configuration used to include auto reconfiguration beans. This should be
 * used when an app uses an AnnotationConfigWebApplicationContext.
 *
 * @author Thomas Risberg
 *
 */
@Configuration
@ImportResource("classpath:META-INF/cloud/cloudfoundry-auto-reconfiguration-context.xml")
public class CloudAppAnnotationConfigAutoReconfig {
}
