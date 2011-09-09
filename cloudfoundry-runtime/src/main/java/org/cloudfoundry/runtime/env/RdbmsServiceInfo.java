package org.cloudfoundry.runtime.env;

import java.util.Map;


/**
 * Information to access RDBMS service.
 *
 * @author Thomas Risberg
 *
 */

public class RdbmsServiceInfo extends AbstractDataSourceServiceInfo {

	private AbstractDataSourceServiceInfo delegate;

	public RdbmsServiceInfo(Map<String, Object> serviceInfo) {
		super(serviceInfo);
		if (getLabel() != null && getLabel().startsWith("postgres")) {
			this.delegate = new PostgresqlServiceInfo(serviceInfo);
		}
		else {
			this.delegate = new MysqlServiceInfo(serviceInfo);
		}

	}

	@Override
	public String getUrl() {
		return delegate.getUrl();
	}
}
