package edu.usc.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import edu.usc.tool.ApkWriter;
import edu.usc.tool.ShellExecutor;
import edu.usc.tool.covert.Architecture;
import edu.usc.tool.covert.StaticAnalysisService;
import edu.usc.tool.covert.Vulnerabilities;

@Path("/")
public class ToolService {

	private static Lock API_LOCK = new ReentrantLock();
	
	@POST
	@Path("/runToolMock")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runToolMock(final InputStream pInData) {
		String myMockJson = "{\"combined_json\": [{\"intentActions\": [],\"theAppName\": \"Echoer\",\"receivers\": [\"org.cert.echoer.MainActivity\"],\"filterActions\": [\"android.intent.action.SEND\"],\"componentNames\": [\"org.cert.echoer.MainActivity\", \"org.cert.echoer.Button1Listener\"],\"senders\": [],\"permissions\": []}, {\"intentActions\": [\"android.intent.action.SEND\", \"android.intent.action.SEND\", \"android.intent.action.SEND\", \"android.intent.action.SEND\"],\"theAppName\": \"SendSMS\",\"receivers\": [\"org.cert.sendsms.MainActivity\"],\"filterActions\": [\"android.intent.action.MAIN\"],\"componentNames\": [\"org.cert.sendsms.MainActivity\", \"org.cert.sendsms.Button1Listener\"],\"senders\": [\"org.cert.sendsms.Button1Listener\", \"org.cert.sendsms.Button1Listener\", \"org.cert.sendsms.Button1Listener\", \"org.cert.sendsms.Button1Listener\"],\"permissions\": [\"READ_PHONE_STATE\", \"SEND_SMS\", \"READ_PHONE_STATE\", \"SEND_SMS\"]}],\"finalOutput\": [{\"theCommonAction\": \"android.intent.action.SEND\",\"Component1\": \"org.cert.sendsms.Button1Listener\",\"Component2\": \"org.cert.echoer.MainActivity\"}]}";
		return Response.status(200).entity(myMockJson).build();
	}

	@POST
	@Path("/runTool")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runTool(final InputStream pInData) {
		return runArchitecture(pInData);
	}

	@POST
	@Path("/runArchitecture")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runArchitecture(final InputStream pInData) {
		try {
			API_LOCK.tryLock(30, TimeUnit.SECONDS);

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
				throw new IllegalStateException(
						myCovertHome.getAbsolutePath() + " COVERT home directory does not exist!");
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
			ShellExecutor.executeDirectory(myCovertHome, "/bin/bash",
					myProperties.getProperty(ToolSettings.PARSER_PATH));

			// retrieve the json "architecture" file
			File myJsonDir = new File(myProperties.getProperty(ToolSettings.JSON_OUTPUT_PATH));

			String myCovertAnalysisKey = toKey(myApkObjectList);

			// store the COVERT architecture
			String myArchitectureJson = Architecture.toJson(myJsonDir);
			Architecture.storeArchitecture(myCovertAnalysisKey, myArchitectureJson);

			// store the COVERT analysis file
			File myCovertAnalysisFile = new File(myApkDirectory, "bundle.xml");
			StaticAnalysisService.storeStaticAnalysis(myCovertAnalysisKey, myCovertAnalysisFile);

			// Return the json "architecture" file
			return Response.status(200).entity(myArchitectureJson).build();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			API_LOCK.unlock();
		}
	}

	@POST
	@Path("/runAnalysis")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runAnalysis(final InputStream pInData) {

		try {
			API_LOCK.tryLock(30, TimeUnit.SECONDS);

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

			String myJson = Architecture.appendVulnerabilities(myKey, myVulnerabilities);
			return Response.status(200).entity(myJson).build();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			API_LOCK.unlock();
		}
	}

	@POST
	@Path("/runAnalysisMock")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runAnalysisMock(final InputStream pInData) {
		String myMockResponse = "{\"combined_json\": [{\"intentActions\": [],\"theAppName\": \"Echoer\",\"receivers\": [\"org.cert.echoer.MainActivity\"],\"filterActions\": [\"android.intent.action.SEND\"],\"componentNames\": [\"org.cert.echoer.MainActivity\", \"org.cert.echoer.Button1Listener\"],\"senders\": [],\"permissions\": []}, {\"intentActions\": [\"android.intent.action.SEND\", \"android.intent.action.SEND\", \"android.intent.action.SEND\", \"android.intent.action.SEND\"],\"theAppName\": \"SendSMS\",\"receivers\": [\"org.cert.sendsms.MainActivity\"],\"filterActions\": [\"android.intent.action.MAIN\"],\"componentNames\": [\"org.cert.sendsms.MainActivity\", \"org.cert.sendsms.Button1Listener\"],\"senders\": [\"org.cert.sendsms.Button1Listener\", \"org.cert.sendsms.Button1Listener\", \"org.cert.sendsms.Button1Listener\", \"org.cert.sendsms.Button1Listener\"],\"permissions\": [\"READ_PHONE_STATE\", \"SEND_SMS\", \"READ_PHONE_STATE\", \"SEND_SMS\"]}],\"finalOutput\": [{\"theCommonAction\": \"android.intent.action.SEND\",\"Component1\": \"org.cert.sendsms.Button1Listener\",\"Component2\": \"org.cert.echoer.MainActivity\"}],\"vulnerabilities\": [{\"attack_type\": \"Intent Spoofing\",\"component_name_1\": \"org.cert.sendsms.MainActivity\"}, {\"attack_type\": \"Intent Spoofing\",\"component_name_1\": \"org.cert.echoer.MainActivity\"}]}";
		return Response.status(200).entity(myMockResponse).build();
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
