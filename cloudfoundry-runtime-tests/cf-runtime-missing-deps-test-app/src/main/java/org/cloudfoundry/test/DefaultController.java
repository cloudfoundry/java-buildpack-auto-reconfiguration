package org.cloudfoundry.test;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that allows basic health check and exposes injected cloud
 * properties
 *
 * @author Jennifer Hickey
 *
 */
@Controller
public class DefaultController {

	@Autowired
	@Qualifier("cloudProperties")
	private Properties cloudProperties;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public String status(HttpServletResponse response) throws IOException {
		return "Hello - the app is up!";
	}

	@RequestMapping(value = "/properties", method = RequestMethod.GET)
	@ResponseBody
	public Properties getCloudProperties() {
		return cloudProperties;
	}
}
