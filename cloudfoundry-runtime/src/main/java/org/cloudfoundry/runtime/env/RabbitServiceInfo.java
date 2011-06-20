package org.cloudfoundry.runtime.env;

import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Information to access RabbitMQ service.
 *
 * @author Ramnivas Laddad
 *
 */

public class RabbitServiceInfo extends AbstractServiceInfo {
	private String host;
	private int port;
	private String password;
	private String virtualHost;
	private String userName;

	public RabbitServiceInfo(Map<String, Object> serviceInfo)
		throws URISyntaxException
	{
		super(serviceInfo);

		@SuppressWarnings("unchecked")
		Map<String,Object> credentials
		    = (Map<String, Object>) serviceInfo.get("credentials");

		// Use the amqp URI in the url property if present.
		// Otherwise, try the broken-ou credentials.
		String uri = (String) credentials.get("url");
		if (uri != null) {
			parseAmqpUri(uri);
		}
		else {
		    host = (String) credentials.get("hostname");
		    port = (Integer) credentials.get("port");
		    password = (String) credentials.get("pass");
		    userName = (String) credentials.get("user");
		    virtualHost = (String) credentials.get("vhost");
		}
	}

	private void parseAmqpUri(String uristr)
		throws URISyntaxException
	{
		URI uri = new URI(uristr);

		if (!"amqp".equals(uri.getScheme()))
			throw new IllegalArgumentException("wrong scheme in amqp URI: " + uristr);

		host = uri.getHost();
		if (host == null)
			throw new IllegalArgumentException("missing authority in amqp URI: " + uristr);

		port = uri.getPort();
		if (port == -1)
			port = 5672;

		String userInfo = uri.getRawUserInfo();
		if (userInfo != null) {
			String userPass[] = userInfo.split(":");
			if (userPass.length != 2)
				throw new IllegalArgumentException("bad user info in amqp URI: " + uristr);

			userName = uriDecode(userPass[0]);
			password = uriDecode(userPass[1]);
		}
		else {
			userName = "guest";
			password = "guest";
		}

		String path = uri.getRawPath();
		if (path == null || path.length() == 0) {
			// The RabbitMQ default vhost
			virtualHost = "/";
		}
		else {
			// Check that the path only has a single
			// segment.  As we have an authority component
			// in the URI, paths always begin with a
			// slash.
			if (path.indexOf('/', 1) != -1)
				throw new IllegalArgumentException("multiple segemtns in path of amqp URI: " + uristr);

			virtualHost = uri.getPath().substring(1);
		}
	}

	private String uriDecode(String s) {
		try {
			// URLDecode decodes '+' to a space, as for
			// form encoding.  So protect plus signs.
			return URLDecoder.decode(s.replace("+", "%2B"),
						 "US-ASCII");
		}
		catch (java.io.UnsupportedEncodingException e) {
			// US-ASCII is always supported
			throw new RuntimeException(e);
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPassword() {
		return password;
	}
	public String getVirtualHost() {
		return virtualHost;
	}

	public String getUserName() {
		return userName;
	}
}
