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

public class GameInfo {
    private String name = new String();
    private String executablePath = new String();
    private String savePath = new String();

    /**
     * Generate empty game info.
     */
    public GameInfo() {
    }

    /**
     * Generate game info.
     * 
     * @param name The name of the game.
     * @param savePath The path to its save game directory.
     * @param executablePath The path to its executable file.
     */
    public GameInfo(String name, String savePath, String executablePath) {
        super();
        this.name = name;
        this.executablePath = executablePath;
        this.savePath = savePath;
    }

    /**
     * Returns the game's name.
     * 
     * @return The game's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the game's name
     * 
     * @param name The game's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the game's executable path.
     * 
     * @return The game's executable path.
     */
    public String getExecutablePath() {
        return executablePath;
    }

    /**
     * Sets the game's executable path.
     * 
     * @param executablePath The game's executable path.
     */
    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    /**
     * Returns the game's save path.
     * 
     * @return The game's save path.
     */
    public String getSavePath() {
        return savePath;
    }

    /**
     * Sets the game's save path.
     * 
     * @param savePath The game's save path.
     */
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

}
