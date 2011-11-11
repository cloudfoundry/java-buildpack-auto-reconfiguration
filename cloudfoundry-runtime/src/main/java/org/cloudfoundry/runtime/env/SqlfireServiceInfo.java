package org.cloudfoundry.runtime.env;

import java.util.Map;

/**
 * Information to access Sqlfire service.
 *
 * @author Jens Deppe
 *
 */
public class SqlfireServiceInfo extends AbstractDataSourceServiceInfo {

    public SqlfireServiceInfo(Map<String, Object> serviceInfo) {
        super(serviceInfo);
    }

    @Override
    public String getUrl() {
        return "jdbc:sqlfire://" + getHost() + ":" + getPort()
                + "/;user=" + getUserName() + ";password=" + getPassword();
    }
}
