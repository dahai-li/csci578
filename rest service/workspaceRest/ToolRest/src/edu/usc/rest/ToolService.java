package edu.usc.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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

@Path("/")
public class ToolService {

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

		// Clean up the COVERT "bundle" directory
		ShellExecutor.executeDirectory(myCovertHome, "cleanUpCovert.sh");

		// Write the APKs to the "bundle" directory
		Gson myGson = new Gson();
		ApkObjectList myApkObjectList = myGson.fromJson(myReceivedData, ApkObjectList.class);

		// write APK file
		List<ApkObject> myApkObjects = myApkObjectList.apks;
		for (ApkObject myApkObject : myApkObjects) {
			ApkWriter.writeApkObjectToFile(myApkObject);
		}

		// run the integration tool script
		ShellExecutor.executeDirectory(myCovertHome, myProperties.getProperty(ToolSettings.PARSER_PATH));

		// retrieve the file
		File myFile = new File(myProperties.getProperty(ToolSettings.JSON_OUTPUT_PATH));

		// get the
		String myVisualizerJson;
		try {
			myVisualizerJson = FileUtils.readFileToString(myFile);
			return Response.status(200).entity(myVisualizerJson).build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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

}
