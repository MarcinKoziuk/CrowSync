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
package nl.koziuk.crowsync.systray;

import nl.koziuk.crowsync.sync.CrowSyncSynchronizer;

/**
 * A simple interface which allows the systray thread to receive tasks
 * from the synchronization thread.
 * 
 * @author marcin
 */
public interface SystrayTask {

    /**
     * Notifies the systray that the synchronization thread has stopped syncing.
     * 
     * @author marcin
     */
    public class StoppedTask implements SystrayTask {

        @Override
        public boolean perform(CrowSyncSystray systray) {
            systray.setStatus(CrowSyncSynchronizer.STOPPED);
            systray.addToLog(new Activity(Activity.STOPPED));
            return true;
        }

    }

    /**
     * Notifies the systray that the synchronization thread has begun syncing.
     * 
     * @author marcin
     */
    public class StartedTask implements SystrayTask {

        @Override
        public boolean perform(CrowSyncSystray systray) {
            systray.setStatus(CrowSyncSynchronizer.RUNNING);
            systray.addToLog(new Activity(Activity.STARTED));
            return true;
        }

    }

    /**
     * Notifies the systray about a restart that has been performed.
     * 
     * @author marcin
     */
    public class RestartedTask implements SystrayTask {

        @Override
        public boolean perform(CrowSyncSystray systray) {
            systray.setStatus(CrowSyncSynchronizer.RUNNING);
            systray.addToLog(new Activity(Activity.RESTARTED));
            return true;
        }

    }

    /**
     * Notifies that files have been sent to the sync folder.
     */
    public class SentTask implements SystrayTask {

        private final Activity activity;

        public SentTask(String gameName, String path) {
            activity = new Activity(Activity.SENT, path, gameName);
        }

        @Override
        public boolean perform(CrowSyncSystray systray) {

            systray.addToLog(activity);
            systray.showMessage("Sent " + activity.getGame() + " save file to sync folder.");
            return true;
        }

    }

    /**
     * Notifies that files have been received from the sync folder.
     */
    public class ReceivedTask implements SystrayTask {

        private final Activity activity;

        public ReceivedTask(String gameName, String path) {
            activity = new Activity(Activity.RECEIVED, path, gameName);
        }

        @Override
        public boolean perform(CrowSyncSystray systray) {

            systray.addToLog(activity);
            systray.showMessage("Received " + activity.getGame() + " save file from sync folder.");
            return true;
        }

    }

    /**
     * Notifies that files have been removed from the sync folder.
     */
    public class RemovedTask implements SystrayTask {

        private final Activity activity;

        public RemovedTask(String gameName, String path) {
            activity = new Activity(Activity.REMOVED, path, gameName);
        }

        @Override
        public boolean perform(CrowSyncSystray systray) {

            systray.addToLog(activity);
            return true;
        }

    }

    /**
     * Notifies the systray thread to stop and also quit the app thread.
     * 
     * @author marcin
     */
    public class ExitTask implements SystrayTask {
        @Override
        public boolean perform(CrowSyncSystray systray) {
            systray.setStatus(CrowSyncSynchronizer.STOPPED);
            systray.addToLog(new Activity(Activity.STOPPED));
            return false;
        }
    }

    /**
     * The task to perform. The return value will be used to decide whether to
     * stop listening for new tasks.
     * 
     * @param systray A reference to the systray object.
     * @return Whether to stop receiving tasks.
     */
    public boolean perform(CrowSyncSystray systray);
}
