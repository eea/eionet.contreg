package eionet.cr.test.util;

import org.dbunit.PropertiesBasedJdbcDatabaseTester;

import eionet.cr.config.GeneralConfig;

public class DbHelper {
	public static void setUpConnectionProperties() {
		System.setProperty(
				PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS,
				GeneralConfig.getProperty(GeneralConfig.DB_DRV));
		System.setProperty(
				PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL,
				GeneralConfig.getProperty(GeneralConfig.DB_URL));
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME,
				GeneralConfig.getProperty(GeneralConfig.DB_USER_ID));
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD,
				GeneralConfig.getProperty(GeneralConfig.DB_USER_PWD));
	}
}
