package edu.usc.tool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import edu.usc.rest.ToolSettings;

public final class MySqlConnectionFactory {

	public static Connection createConnection() throws SQLException {
		Properties myProperties = ToolSettings.getProperties();
		MysqlDataSource myDataSource = new MysqlDataSource();
		myDataSource.setUser(myProperties.getProperty(ToolSettings.MYSQL_USER));
		myDataSource.setPassword(myProperties.getProperty(ToolSettings.MYSQL_PASSWORD));
		myDataSource.setDatabaseName(myProperties.getProperty(ToolSettings.MYSQL_DATABASE));
		myDataSource.setServerName(myProperties.getProperty(ToolSettings.MYSQL_SERVER));
		myDataSource.setPort(Integer.valueOf(myProperties.getProperty(ToolSettings.MYSQL_PORT)));
		return myDataSource.getConnection();
	}

	public static void closeConnectionQuietly(final Connection pConnection) {
		if (pConnection == null) {
			return;
		}
		try {
			pConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private MySqlConnectionFactory() {
	}
}
