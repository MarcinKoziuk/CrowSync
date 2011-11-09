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
package nl.koziuk.crowsync;

import java.awt.EventQueue;

import nl.koziuk.crowsync.systray.CrowSyncSystray;

/**
 * Game save synchronizer for dropbox or other desktop synchronization
 * software.
 * 
 * @author marcin
 */
public class CrowSync {
    public static final String PROGRAM_NAME = "CrowSync";
    public static final String PROGRAM_SYNC_NAME = "CrowSync synchronization service";
    public static final String PROGRAM_DESCRIPTION = "Game save synchronizer for desktop cloud services.";
    public static final String VERSION = "0.1.0";
    public static final String[] AUTHORS = { "Marcin Koziuk" };
    public static final String HOMEPAGE = "http://koziuk.nl/crowsync/";
    public static final String GITHUB_HOMEPAGE = "https://github.com/MarcinKoziuk/crowsync/";
    public static final String SUPPORT_EMAIL = "marcin.koziuk@gmail.com";
    public static final String CONFIG_FILENAME = "crowsync.properties";
    public static final String LOG_FILENAME = "crowsync.log";
    public static final String GAMES_FILENAME = "games.xml";

    /**
     * Starts the system tray (monitor thread).
     * 
     * @param args The command line arguments are currently unused.
     */
    public static void main(String[] args) {

        try {
            new CrowSyncSystray(args);

            // multiple catch clauses due to ErrorDialog constructor overloading
        } catch (final CrowSyncException e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ErrorDialog(true, e).setVisible(true);
                }
            });
        } catch (final Exception e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ErrorDialog(true, e).setVisible(true);
                }
            });
        }
    }

}
