package edu.usc.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import edu.usc.tool.ApkWriter;
import edu.usc.tool.ShellExecutor;
import edu.usc.tool.covert.StaticAnalysisService;
import edu.usc.tool.covert.Vulnerabilities;

@Path("/")
public class ToolService {

	@POST
	@Path("/runToolMock")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runToolMock(final InputStream pInData) {
		String myMockJson = "{\"interacting Components\": [[\"org.cert.sendsms.Button1Listener\", \"org.cert.echoer.MainActivity\"], [\"org.cert.sendsms.Button1Listener\", \"org.cert.echoer.MainActivity\"], [\"org.cert.sendsms.Button1Listener\", \"org.cert.echoer.MainActivity\"], [\"org.cert.sendsms.Button1Listener\", \"org.cert.echoer.MainActivity\"]], \"Permissions\": [[\"SendSMS\", \"READ_PHONE_STATE\"], [\"SendSMS\", \"SEND_SMS\"]]}";
		return Response.status(200).entity(myMockJson).build();
	}

	@POST
	@Path("/runTool")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runTool(final InputStream pInData) {
		String myReceivedData;
		try {
			myReceivedData = IOUtils.toString(pInData);
			System.out.println("Data Received: " + myReceivedData);
		} catch (IOException e1) {
			throw new RuntimeException("Could not read input stream", e1);
		}

		Properties myProperties = ToolSettings.getProperties();

		String myCovertHomePath = myProperties.getProperty(ToolSettings.COVERT_HOME);
		File myCovertHome = new File(myCovertHomePath);
		if (!myCovertHome.exists()) {
			throw new IllegalStateException(myCovertHome.getAbsolutePath() + " COVERT home directory does not exist!");
		}

		String myApkDirectoryPath = myProperties.getProperty(ToolSettings.APK_UPLOAD);
		File myApkDirectory = new File(myApkDirectoryPath);
		if (!myApkDirectory.exists()) {
			throw new IllegalStateException(
					myApkDirectory.getAbsolutePath() + " " + ToolSettings.APK_UPLOAD + " does not exist!");
		}

		// Clean up the COVERT "bundle" directory
		ShellExecutor.executeDirectory(myCovertHome, "/bin/bash", "cleanUpCovert.sh");

		// Write the APKs to the "bundle" directory
		Gson myGson = new Gson();
		ApkObjectList myApkObjectList = myGson.fromJson(myReceivedData, ApkObjectList.class);

		// write APK file
		List<ApkObject> myApkObjects = myApkObjectList.apks;
		for (ApkObject myApkObject : myApkObjects) {
			ApkWriter.writeApkObjectToFile(myApkObject);
		}

		// run the integration tool script
		ShellExecutor.executeDirectory(myCovertHome, "/bin/bash", myProperties.getProperty(ToolSettings.PARSER_PATH));

		// retrieve the json "architecture" file
		File myFile = new File(myProperties.getProperty(ToolSettings.JSON_OUTPUT_PATH));

		// store the COVERT analysis file
		String myCovertAnalysisKey = toKey(myApkObjectList);
		File myCovertAnalysisFile = new File(myApkDirectory, "bundle.xml");
		StaticAnalysisService.storeStaticAnalysis(myCovertAnalysisKey, myCovertAnalysisFile);

		// Return the json "architecture" file
		String myVisualizerJson;
		try {
			myVisualizerJson = FileUtils.readFileToString(myFile);
			return Response.status(200).entity(myVisualizerJson).build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

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

		Vulnerabilities myVulnerabilities = StaticAnalysisService.parseKey(myKey);

		String myJson = myGson.toJson(myVulnerabilities);
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
