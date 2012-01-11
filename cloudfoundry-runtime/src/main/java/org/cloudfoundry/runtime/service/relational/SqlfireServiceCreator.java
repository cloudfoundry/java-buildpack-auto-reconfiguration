package org.cloudfoundry.runtime.service.relational;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.SqlfireServiceInfo;
import org.cloudfoundry.runtime.service.AbstractDataSourceCreator;

/**
 * Simplified access to Sqlfire service.
 *
 * @author Jens Deppe
 *
 */
public class SqlfireServiceCreator extends AbstractDataSourceCreator<SqlfireServiceInfo> {

    private static final String SQLFIRE_DRIVER_CLASS_NAME = "com.vmware.sqlfire.jdbc.ClientDriver";
    private static final String VALIDATION_QUERY = "SELECT current_timestamp FROM sysibm.sysdummy1";

    @Override
    public String getDriverClassName() {
        return SQLFIRE_DRIVER_CLASS_NAME;
    }

    @Override
    public String getValidationQuery() {
        return VALIDATION_QUERY;
    }
}
