package edu.usc.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ToolSettings {

	private static final String PROPERTIES_PATH = "/edu/usc/rest/settings.properties";

	public static final String DIDFAIL_HOME = "didfail_home";
	public static final String APK_UPLOAD = "apk_upload";
	public static final String MYSQL_USER = "mysql_user";
	public static final String MYSQL_PASSWORD = "mysql_password";
	public static final String MYSQL_DATABASE = "mysql_database";
	public static final String PARSER_PATH = "parser_path";
	public static final String JSON_OUTPUT_PATH = "json_output_path";
	public static final String MYSQL_SERVER = "mysql_server";
	public static final String MYSQL_PORT = "mysql_port";

	private static Properties sProperties = null;

	public static final Properties getProperties() {
		if (sProperties != null) {
			return sProperties;
		}

		InputStream myIs = ToolSettings.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
		if (myIs == null) {
			throw new IllegalStateException("Properties file is not at: " + PROPERTIES_PATH);
		}

		sProperties = new Properties();
		try {
			sProperties.load(myIs);
		} catch (IOException e) {
			throw new RuntimeException("Could not load properties file at: " + PROPERTIES_PATH, e);
		}
		return sProperties;
	}

	private ToolSettings() {
	}
}
