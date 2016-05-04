package edu.usc.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.usc.tool.ApkWriter;
import edu.usc.tool.ShellExecutor;
import edu.usc.tool.covert.Architecture;
import edu.usc.tool.covert.Vulnerabilities;
import edu.usc.tool.covert.Vulnerability;

@Path("/")
public class ToolService {

	@POST
	@Path("/runAnalysis")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runAnalysis(final InputStream pInData) {
		String myReceivedData;
		try {
			myReceivedData = IOUtils.toString(pInData);
			System.out.println("Data Received: " + myReceivedData);
		} catch (IOException e1) {
			throw new RuntimeException("Could not read input stream", e1);
		}
		Gson myGson = new Gson();
		ApkObjectList myApkObjectList = myGson.fromJson(myReceivedData, ApkObjectList.class);

		String myKey = toKey(myApkObjectList);

		Properties myProperties = ToolSettings.getProperties();
		File myDirectory = new File(myProperties.getProperty(ToolSettings.DIDFAIL_HOME));
		if (!myDirectory.exists()) {
			throw new IllegalStateException("The directory: " + myDirectory.getAbsolutePath() + " does not exist.");
		}
		// clean up environment
		ShellExecutor.executeDirectory(myDirectory, "/bin/bash", "cleanUp.sh");

		// write APKs to directory
		List<ApkObject> myApkObjects = myApkObjectList.apks;
		for (ApkObject myApkObject : myApkObjects) {
			ApkWriter.writeApkObjectToFile(myApkObject);
		}

		// execute static analysis
		ShellExecutor.executeDirectory(myDirectory, "/bin/bash", "runDidFail.sh");

		// Pick up the Vulnerabilities json
		Vulnerabilities myVulnerabilities = new Vulnerabilities();
		myVulnerabilities.vulnerabilities = new ArrayList<Vulnerability>();
		File myVulnerabilitiesJson = new File(myProperties.getProperty(ToolSettings.JSON_OUTPUT_PATH));
		if (myVulnerabilitiesJson.exists()) {
			try {
				String myVulnerabilitiesStr = FileUtils.readFileToString(myVulnerabilitiesJson);
				Type listType = new TypeToken<ArrayList<Vulnerability>>() {
				}.getType();
				ArrayList<Vulnerability> myList = myGson.fromJson(myVulnerabilitiesStr, listType);
				myVulnerabilities.vulnerabilities = myList;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		String myJson = Architecture.appendVulnerabilities(myKey, myVulnerabilities);
		return Response.status(200).entity(myJson).build();
	}

	@GET
	@Path("/getProperties")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProperties(final InputStream pInData) {
		Properties myProperties = ToolSettings.getProperties();
		Gson myGson = new Gson();
		String myJson = myGson.toJson(myProperties);
		return Response.status(200).entity(myJson).build();
	}

	private static String toKey(final ApkObjectList pKey) {
		Set<String> myKey = new TreeSet<String>();
		for (ApkObject myApkObject : pKey.apks) {
			myKey.add(myApkObject.id);
		}
		return myKey.toString();
	}

}
