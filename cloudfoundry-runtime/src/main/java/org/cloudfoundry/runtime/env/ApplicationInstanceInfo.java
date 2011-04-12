package org.cloudfoundry.runtime.env;

import java.util.List;
import java.util.Map;

/**
 * 
 * Examines VCAP_APPLICATION environment variable to provide application instance information. 
 * <p>
 * This class expects the value for the environment variable in the following JSON form
 * <pre>
 * {
 *	"instance_id":"4057662a40caae017ac86fb3ab12f1a2",
 *	"instance_index":0,
 *	"name":"env",
 *	"uris":["env.vcap.me"],
 *	"users":["user@company.com"],
 *	"version":"8364547a15da0dd39aa8f62f0497ae1598bdd037",
 *	"start":"2011-04-02 11:39:11 -0700",
 *	"runtime":"ruby18",
 *	"state_timestamp":1301769551,
 *	"port":53160,
 *	"limits":{
 *		"fds":256,
 *		"mem":134217728,
 *		"disk":2147483648
 *	},
 *	"host":"127.0.0.1"
 *}
 *</code>
 * 
 * @author Ramnivas Laddad
 *
 */
public class ApplicationInstanceInfo {
	private String name;
	private int instanceIndex;
	private List<String> uris;
	
	private String host;
	private int port;
	
	@SuppressWarnings("unchecked")
	public ApplicationInstanceInfo(Map<String, Object> info) {
		name = (String) info.get("name");
		instanceIndex = (Integer) info.get("instance_index");
		
		uris = (List<String>) info.get("uris");
		
		host = (String) info.get("host");
		port = (Integer) info.get("port");
	}

	public String getName() {
		return name;
	}

	public int getInstanceIndex() {
		return instanceIndex;
	}

	public List<String> getUris() {
		return uris;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	
}



