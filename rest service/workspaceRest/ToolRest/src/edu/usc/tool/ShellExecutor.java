package edu.usc.tool;

import java.io.File;
import java.util.Arrays;

public final class ShellExecutor {

	public static void execute(String... pCommands) {
		try {
			System.out.println("Executing command: " + Arrays.toString(pCommands));
			ProcessBuilder myPb = new ProcessBuilder().inheritIO().command(pCommands);
			Process myProcess = myPb.start();
			myProcess.waitFor();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not execute: " + Arrays.toString(pCommands), e);
		}
	}

	public static void executeDirectory(File pDirectory, String... pCommands) {
		try {
			System.out.println("Executing command: " + Arrays.toString(pCommands));
			ProcessBuilder myPb = new ProcessBuilder().inheritIO().command(pCommands);
			myPb.directory(pDirectory);
			Process myProcess = myPb.start();
			myProcess.waitFor();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not execute: " + Arrays.toString(pCommands), e);
		}
	}

	private ShellExecutor() {
	}
}
