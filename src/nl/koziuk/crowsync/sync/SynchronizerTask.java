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
package nl.koziuk.crowsync.sync;

import nl.koziuk.crowsync.persist.ConfigFile;
import nl.koziuk.crowsync.persist.GameFile;
import nl.koziuk.crowsync.systray.SystrayTask.RestartedTask;
import nl.koziuk.crowsync.systray.SystrayTask.StartedTask;
import nl.koziuk.crowsync.systray.SystrayTask.StoppedTask;

/**
 * A simple interface which allows the synchronization thread to receive tasks
 * from the EDT and systray thread.
 * 
 * @author marcin
 */
public interface SynchronizerTask {

    /**
     * Asks the synchronization thread to start synchronizing.
     * 
     * @author marcin
     */
    public static class StartTask implements SynchronizerTask {
        protected final ConfigFile configFile;
        protected final GameFile gameFile;

        public StartTask(ConfigFile configFile, GameFile gameFile) {
            this.configFile = configFile;
            this.gameFile = gameFile;
        }

        protected void updateSynchronizer(CrowSyncSynchronizer crowSync) {
            synchronized (configFile) {
                synchronized (gameFile) {
                    crowSync.loadConfig(configFile, gameFile);
                }
            }
            crowSync.setRunning(true);
        }

        @Override
        public boolean perform(CrowSyncSynchronizer crowSync) throws InterruptedException {
            updateSynchronizer(crowSync);
            crowSync.getSystrayQueue().put(new StartedTask());
            return true;
        }

    }

    /**
     * Stops syncing, but will still listen for new tasks.
     * 
     * @author marcin
     */
    public static class StopTask implements SynchronizerTask {
        @Override
        public boolean perform(CrowSyncSynchronizer crowSync) throws InterruptedException {
            crowSync.setRunning(false);
            crowSync.getSystrayQueue().put(new StoppedTask());
            return true;
        }

    }

    /**
     * Asks for the systray process to restart. This will not actually stop
     * the tread but just reload its config.
     * 
     * @author marcin
     */
    public static class RestartTask extends StartTask implements SynchronizerTask {
        public RestartTask(ConfigFile configFile, GameFile gameFile) {
            super(configFile, gameFile);
        }

        @Override
        public boolean perform(CrowSyncSynchronizer crowSync) throws InterruptedException {
            updateSynchronizer(crowSync);
            crowSync.getSystrayQueue().put(new RestartedTask());
            return true;
        }

    }

    /**
     * Stops the sync thread entirely, which will in turn lead to the stop of
     * all other threads.
     * 
     * @author marcin
     */
    public static class ExitTask implements SynchronizerTask {
        @Override
        public boolean perform(CrowSyncSynchronizer crowSync) {
            return false;
        }
    }

    /**
     * The task to perform. The return value will be used to decide whether to
     * stop listening for new tasks.
     * 
     * @param crowSync A reference to the synchronization object.
     * @return Whether to stop receiving tasks.
     * @throws InterruptedException
     */
    public boolean perform(CrowSyncSynchronizer crowSync) throws InterruptedException;
}
