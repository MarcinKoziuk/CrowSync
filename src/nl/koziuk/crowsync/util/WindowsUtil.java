package nl.koziuk.crowsync.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Source: http://www.rgagnon.com/javadetails/java-0593.html
 * 
 * @author marcin
 * @author Réal Gagnon
 */
public class WindowsUtil {

	// TODO: make linux version with the ps command

	/**
	 * Returns list of running processes. Only works on windows.
	 * 
	 * @return List of currently running processes.
	 */
	public static Set<String> listRunningProcesses() {

		Set<String> processes = new HashSet<String>();

		try {
			String line;
			Process p = Runtime.getRuntime().exec("tasklist.exe /fo csv /nh");
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			while ((line = input.readLine()) != null) {
				if (!line.trim().equals("")) {
					// keep only the process name
					line = line.substring(1);
					processes.add(line.substring(0, line.indexOf("\"")));
				}

			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return processes;
	}

	/**
	 * Because Java 6 doesn't support file copying.
	 * 
	 * @param sourceFile
	 *            The source file.
	 * @param destDir
	 *            The destination file.
	 * @return Whether the copying succeeded or not.
	 */
	public static boolean copyFileToDir(File sourceFile, File destDir) {
		if (!sourceFile.exists() | !sourceFile.isFile()
				| !destDir.isDirectory()) {
			throw new IllegalArgumentException();
		}

		try {
			Process process = Runtime.getRuntime().exec(
					new String[] { "cmd", "/c", "copy",
							sourceFile.getAbsolutePath(),
							destDir.getAbsolutePath() });
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			return false;
		}

		return true;
	}
}