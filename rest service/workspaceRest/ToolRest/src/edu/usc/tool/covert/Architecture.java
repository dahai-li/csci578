package edu.usc.tool.covert;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import edu.usc.tool.MySqlConnectionFactory;

public class Architecture {

	public static void main(String[] args) throws IOException {
		String myJson = toJson(new File(args[0]));
		System.out.println(myJson);
	}

	public static String toJson(final File pJsonDirectory) {
		String myFinalOutputJson = null;
		StringBuilder myCombinedJson = new StringBuilder("{");

		myCombinedJson.append("\"combined_json\": [");

		boolean isFirst = true;
		try {
			for (File myFile : pJsonDirectory.listFiles()) {
				if (myFile.getName().endsWith(".json")) {
					if (myFile.getName().equals("finalOutput.json")) {
						myFinalOutputJson = FileUtils.readFileToString(myFile);
						continue;
					}
					if (isFirst) {
						isFirst = false;
					} else {
						myCombinedJson.append(",");
					}
					myCombinedJson.append(FileUtils.readFileToString(myFile));

				}
			}
		} catch (IOException e) {
			throw new RuntimeException(pJsonDirectory.getAbsolutePath(), e);
		}

		myCombinedJson.append("],\"finalOutput\":");
		if (myFinalOutputJson == null) {
			myCombinedJson.append("[]");
		} else {
			myCombinedJson.append(myFinalOutputJson);
		}

		myCombinedJson.append("}");
		return myCombinedJson.toString();
	}
	
	public static void storeArchitecture(final String pKey, final String pArchitectureJson) {
		// check if it always exists
		String myArchJson = getArchitecture(pKey);
		if (myArchJson != null) {
			return;
		}

		Connection myConnection = null;
		try {
			myConnection = MySqlConnectionFactory.createConnection();
			String myQuery = "INSERT INTO architectures (arch_key, architecture) values (?,?)";
			PreparedStatement myStatement = myConnection.prepareStatement(myQuery);
			myStatement.setString(1, pKey);
			myStatement.setString(2, pArchitectureJson);
			myStatement.execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			MySqlConnectionFactory.closeConnectionQuietly(myConnection);
		}
	}

	public static String getArchitecture(final String pKey) {
		Connection myConnection = null;
		try {
			myConnection = MySqlConnectionFactory.createConnection();
			ResultSet myResultSet = myConnection.createStatement()
					.executeQuery("SELECT architecture from architectures where arch_key = '" + pKey + "'");
			if (myResultSet.next()) {
				String myJson = myResultSet.getString(1);
				return myJson;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			MySqlConnectionFactory.closeConnectionQuietly(myConnection);
		}
	}

	public static String appendVulnerabilities(final String pKey, final Vulnerabilities pVulnerability) {
		Gson myGson = new Gson();
		List<Vulnerability> myVulnerabilities = pVulnerability.vulnerabilities;
		String myVulnerabilitiesJson = myGson.toJson(myVulnerabilities);

		String myArchString = getArchitecture(pKey);
		if (myArchString == null) {
			throw new IllegalStateException(
					"No architecture found for: " + pKey + " please run the architecture analysis first");
		}

		int myEndBracket = myArchString.lastIndexOf("}");
		StringBuilder mySb = new StringBuilder(myArchString.substring(0, myEndBracket));
		mySb.append(",\"vulnerabilities\": ").append(myVulnerabilitiesJson).append("}");
		return mySb.toString();
	}
}
