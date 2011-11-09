/*
 * Copyright (c) 2011, Marcin Koziuk <marcin.koziuk@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package nl.koziuk.crowsync.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * For loading and saving of the flat crowsync.properties configuration file.
 * 
 * TODO: re-implement using composition instead of inheritance
 * 
 * @author marcin
 */
public class ConfigFile extends Properties {

	private final String filename;

	private static final int DEFAULT_MAX_SAVES_PER_GAME = 5;

	private int maxSavesPerGame = DEFAULT_MAX_SAVES_PER_GAME;
	private String syncFolderPath = new String();

	/**
	 * Creates a new ConfigParser that will load a file using a filename. If the
	 * file doesn't already exist, it will be created.
	 * 
	 * @param filename
	 *            The path to the file to load.
	 * @throws IOException
	 */
	public ConfigFile(String filename) throws IOException {
		this.filename = filename;

		FileInputStream in = null;

		try {
			in = new FileInputStream(filename);
			load(in);
		} catch (FileNotFoundException e) {
			File f;
			f = new File(filename);
			if (!f.exists()) {
				f.createNewFile();
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// om nom nom!
				}
			}
		}

		loadConfig();
	}

	/**
	 * Saves the configuration file.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		saveConfig();

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(filename);
			store(out, "");
		} catch (FileNotFoundException e) {
			File f;
			f = new File(filename);
			if (!f.exists()) {
				f.createNewFile();
				out = new FileOutputStream(f);
				store(out, "");
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// om nom nom!
				}
			}
		}
	}

	/**
	 * Returns the maximum number of save files for every game.
	 * 
	 * @return Max save files for each game.
	 */
	public int getMaxSavesPerGame() {
		return maxSavesPerGame;
	}

	/**
	 * Sets the maximum number of save files for every game.
	 * 
	 * @param maxSavesPerGame
	 *            The variable to set.
	 */
	public void setMaxSavesPerGame(int maxSavesPerGame) {
		this.maxSavesPerGame = maxSavesPerGame;
	}

	/**
	 * Returns a directory inside the Dropbox/Ubuntu One/Etc. folder
	 * 
	 * @return The game synchronization directory.
	 */
	public String getSyncFolderPath() {
		return syncFolderPath;
	}

	/**
	 * Sets the sync folder directory.
	 * 
	 * @param syncFolderPath
	 *            Path to the game save sync folder.
	 */
	public void setSyncFolderPath(String syncFolderPath) {
		this.syncFolderPath = syncFolderPath;
	}

	/**
	 * Convenience method that calls getProperty and converts its value to an
	 * integer.
	 * 
	 * @param property
	 *            The property to get.
	 * @param defaultValue
	 *            The property's default value.
	 * @return The property value.
	 */
	private int getIntProperty(String property, int defaultValue) {
		int result;
		try {
			result = Integer.parseInt(getProperty(property,
					Integer.toString(defaultValue)));
		} catch (NumberFormatException e) {
			result = defaultValue;
		}
		return result;
	}

	/**
	 * Convenience method to save an int using setProperty.
	 * 
	 * @param property
	 *            The property to set.
	 * @param value
	 *            Its value.
	 */
	private void setIntProperty(String property, int value) {
		setProperty(property, Integer.toString(value));
	}

	/**
	 * Loads config properties into attributes.
	 */
	private void loadConfig() {
		syncFolderPath = getProperty("sync-folder-path", "");
		maxSavesPerGame = getIntProperty("max-saves-per-game",
				DEFAULT_MAX_SAVES_PER_GAME);
	}

	/**
	 * Saves attributes into configuration properties.
	 */
	private void saveConfig() {
		setProperty("sync-folder-path", syncFolderPath);
		setIntProperty("max-saves-per-game", maxSavesPerGame);
	}

}
