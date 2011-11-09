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

import java.util.concurrent.BlockingQueue;

import javax.swing.ImageIcon;

import nl.koziuk.crowsync.CrowSyncException;
import nl.koziuk.crowsync.sync.SynchronizerTask.ExitTask;
import nl.koziuk.crowsync.sync.SynchronizerTask.RestartTask;
import nl.koziuk.crowsync.sync.SynchronizerTask.StartTask;
import nl.koziuk.crowsync.sync.SynchronizerTask.StopTask;
import nl.koziuk.crowsync.systray.CrowSyncSystray;
import nl.koziuk.crowsync.util.IconUtil;

/**
 * Shared code of both CrowSyncSystray and CrowSyncApp.
 * 
 * @author marcin
 */
public abstract class SynchronizerMonitor {
    protected static final ImageIcon PROGRAM_ICON = IconUtil.imageIcon("trayicon.png");
    protected static final ImageIcon START_ICON = IconUtil.imageIcon("16x16/start.png");
    protected static final ImageIcon STOP_ICON = IconUtil.imageIcon("16x16/stop.png");
    protected static final ImageIcon RESTART_ICON = IconUtil.imageIcon("16x16/restart.png");

    /**
     * Action of the Start menu item.
     */
    protected void startAction() {
        sendSyncTask(new StartTask(getSystray().getConfig(), getSystray().getGameFile()));
    }

    /**
     * Action of the Stop menu item.
     */
    protected void stopAction() {
        sendSyncTask(new StopTask());

    }

    /**
     * Action of teh Restart menu item.
     */
    protected void restartAction() {
        sendSyncTask(new RestartTask(getSystray().getConfig(), getSystray().getGameFile()));

    }

    /**
     * Action of the Exit menu item.
     */
    protected void exitAction() {
        sendSyncTask(new ExitTask());
    }

    /**
     * Return a reference to the systray.
     * 
     * @return The systray.
     */
    protected abstract CrowSyncSystray getSystray();

    /**
     * Return a reference to the blockingqueue.
     * 
     * @return The blocking queue.
     */
    protected abstract BlockingQueue<SynchronizerTask> getSyncQueue();

    /**
     * Helper method for sending tasks to the synchronizer thread.
     * 
     * @param task The sync task.
     */
    protected void sendSyncTask(SynchronizerTask task) {
        try {
            getSyncQueue().put(task);
        } catch (InterruptedException e) {
            throw new CrowSyncException("Could not send " + task.getClass() + " because the sync thread was interrupted.", e);
        }
    }
}
