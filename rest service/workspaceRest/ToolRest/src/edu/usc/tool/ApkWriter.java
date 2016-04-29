package edu.usc.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import edu.usc.rest.ApkObject;
import edu.usc.rest.ToolSettings;

public final class ApkWriter {

	public static void writeApkObjectToFile(final ApkObject pApkObject) {
		writeApkFiles(pApkObject.id);
	}

	private static void writeApkFiles(final String pId) {
		Properties myProperties = ToolSettings.getProperties();
		MysqlDataSource myDataSource = new MysqlDataSource();
		myDataSource.setUser(myProperties.getProperty(ToolSettings.MYSQL_USER));
		myDataSource.setPassword(myProperties.getProperty(ToolSettings.MYSQL_PASSWORD));
		myDataSource.setDatabaseName(myProperties.getProperty(ToolSettings.MYSQL_DATABASE));

		File myDirectory = new File(myProperties.getProperty(ToolSettings.APK_UPLOAD));

		Connection myConnection = null;
		try {
			myConnection = myDataSource.getConnection();
			Statement myStatement = myConnection.createStatement();

			String myQuery = "SELECT name,data from apks where id = " + pId;
			System.out.println("QUERY: " + myQuery);

			ResultSet myResultSet = myStatement.executeQuery(myQuery);

			File myOutputFile = new File(myDirectory, myResultSet.getString("name"));

			System.out.println("writing APK file: " + myOutputFile.getAbsolutePath());

			FileOutputStream myFileOutputStream = new FileOutputStream(myOutputFile);
			IOUtils.copy(myResultSet.getBinaryStream("data"), myFileOutputStream);

			myFileOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (myConnection != null) {
				try {
					myConnection.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
