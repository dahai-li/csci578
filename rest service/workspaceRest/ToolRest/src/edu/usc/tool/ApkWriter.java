package edu.usc.tool;

import edu.usc.rest.ApkObject;

public final class ApkWriter {

	public static void writeApkObjectToFile(final ApkObject pApkObject) {

		System.out.println("writing APK Object name: " + pApkObject.name);
		System.out.println("writing APK Object data: " + pApkObject.data);
	}
}
