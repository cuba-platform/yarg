package com.haulmont.yarg.console;

import javax.sql.DataSource;

/**
 * @author degtyarjov
 * @version $Id$
 */
public final class DatasourceHolder {
    private DatasourceHolder() {
    }

    public static volatile DataSource dataSource;
}
