package edu.usc.tool.covert;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jaxen.JaxenException;

import edu.usc.tool.MySqlConnectionFactory;

/**
 * COVERT tool static analysis
 * @author dahaili
 *
 */
public final class StaticAnalysisService {

	public static Vulnerabilities parseFile(final File pXML) {
		String myXML;
		try {
			myXML = FileUtils.readFileToString(pXML);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parseString(myXML);
	}

	public static Vulnerabilities parseKey(final String pKey) {
		String myAnalysisXml = getAnalysisXml(pKey);
		if (myAnalysisXml == null) {
			Vulnerabilities myVulnerabilities = new Vulnerabilities();
			myVulnerabilities.vulnerabilities = new ArrayList<Vulnerability>();
			return myVulnerabilities;
		} else {
			return parseString(myAnalysisXml);
		}
	}

	private static String getAnalysisXml(final String pKey) {
		Connection myConnection = null;
		try {
			myConnection = MySqlConnectionFactory.createConnection();
			ResultSet myResultSet = myConnection.createStatement()
					.executeQuery("SELECT xml from xmls where xml_key = '" + pKey + "'");
			if (myResultSet.next()) {
				String myXmlString = myResultSet.getString(1);
				return myXmlString;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			MySqlConnectionFactory.closeConnectionQuietly(myConnection);
		}
	}

	private static Vulnerabilities parseString(final String pXML) {

		StringReader myReader = new StringReader(pXML);
		try {
			OMElement myRoot = OMXMLBuilderFactory.createOMBuilder(myReader).getDocumentElement();

			AXIOMXPath myXPath = new AXIOMXPath("/analysisReport/vulnerabilities/vulnerability");

			List<OMElement> myVulnerabilityNodes = myXPath.selectNodes(myRoot);

			Vulnerabilities myVulnerabilities = new Vulnerabilities();
			List<Vulnerability> myVulnerabilityList = new ArrayList<Vulnerability>();
			myVulnerabilities.vulnerabilities = myVulnerabilityList;

			for (OMElement myVulnerability : myVulnerabilityNodes) {
				Vulnerability myVulEle = new Vulnerability();

				String myAttackType = getVulnerabilityType(myVulnerability);
				String myComponentName = getComponentName1(myVulnerability);

				if (myAttackType != null && myComponentName != null) {
					myVulEle.attack_type = myAttackType;
					myVulEle.component_name_1 = myComponentName;
					myVulEle.component_name_2 = getComponentName2(myVulnerability);

					myVulnerabilityList.add(myVulEle);
				}

			}
			return myVulnerabilities;
		} catch (JaxenException e) {
			throw new IllegalStateException(pXML, e);
		} finally {
			IOUtils.closeQuietly(myReader);
		}
	}

	public static void storeStaticAnalysis(final String pKey, final File pXmlFile) {
		// check if it always exists
		String myAnalysisXml = getAnalysisXml(pKey);
		if (myAnalysisXml != null) {
			return;
		}

		Connection myConnection = null;
		try {
			myConnection = MySqlConnectionFactory.createConnection();
			String myQuery = "INSERT INTO xmls (xml_key, xml) values (?,?)";
			PreparedStatement myStatement = myConnection.prepareStatement(myQuery);
			myStatement.setString(1, pKey);
			myStatement.setString(2, FileUtils.readFileToString(pXmlFile));
			myStatement.execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			MySqlConnectionFactory.closeConnectionQuietly(myConnection);
		}
	}

	private StaticAnalysisService() {
	}

	private static String getVulnerabilityType(final OMElement pVulnerability) {
		return executeXPathForElementText(pVulnerability, "./type");
	}

	private static String getComponentName1(final OMElement pVulnerability) {
		return executeXPathForElementText(pVulnerability, "./vulnerabilityElements/element[1]/description");
	}

	private static String getComponentName2(final OMElement pVulnerability) {
		return executeXPathForElementText(pVulnerability, "./vulnerabilityElements/element[2]/description");
	}

	private static String executeXPathForElementText(final OMNode pNode, final String pXPath) {
		AXIOMXPath myXPath;
		try {
			myXPath = new AXIOMXPath(pXPath);
			List<OMElement> myNodes = myXPath.selectNodes(pNode);

			int mySize = myNodes.size();
			if (mySize == 0) {
				return null;
			} else if (mySize == 1) {
				return myNodes.get(0).getText();
			} else {
				throw new IllegalStateException(pNode.toString() + ":: Path: " + pXPath);
			}
		} catch (JaxenException e) {
			throw new IllegalStateException(pNode.toString() + ":: Path: " + pXPath);
		}

	}
}
