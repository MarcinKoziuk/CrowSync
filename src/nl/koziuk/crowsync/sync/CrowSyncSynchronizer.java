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

import java.awt.EventQueue;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import nl.koziuk.crowsync.CrowSyncException;
import nl.koziuk.crowsync.ErrorDialog;
import nl.koziuk.crowsync.persist.ConfigFile;
import nl.koziuk.crowsync.persist.GameFile;
import nl.koziuk.crowsync.persist.GameInfo;
import nl.koziuk.crowsync.systray.SystrayTask;
import nl.koziuk.crowsync.systray.SystrayTask.ExitTask;
import nl.koziuk.crowsync.systray.SystrayTask.ReceivedTask;
import nl.koziuk.crowsync.systray.SystrayTask.RemovedTask;
import nl.koziuk.crowsync.systray.SystrayTask.SentTask;
import nl.koziuk.crowsync.util.OSUtil;
import nl.koziuk.crowsync.util.WindowsUtil;

public class CrowSyncSynchronizer implements Runnable {
    public static final int RUNNING = 0;
    public static final int STOPPED = 1;
    public static final int NOT_RESPONDING = 2;

    // for pushing tasks to other threads
    private final BlockingQueue<SystrayTask> systrayQueue;
    private final BlockingQueue<SynchronizerTask> syncQueue;

    private String syncFolderPath;
    private int maxGameSaves;
    private List<GameInfo> gameList;

    private boolean running = false;

    /**
     * Creates a new synchronization Runnable that may be started by a
     * thread.
     * 
     * @param systrayQueue The blocking queue of the systray.
     * @param syncQueue The blocking queue to use for the synchronizer's
     *            messages.
     */
    public CrowSyncSynchronizer(BlockingQueue<SystrayTask> systrayQueue, BlockingQueue<SynchronizerTask> syncQueue) {
        super();
        this.systrayQueue = systrayQueue;
        this.syncQueue = syncQueue;
    }

    /**
     * Returns whether the save folder has this file or a newer version from it,
     * or not.
     * 
     * @return True of the save folder contains this file or a newer version of
     *         it, otherwise false.
     */
    private static boolean needsNewerFile(GameInfo game, String[] saveDirPaths, File syncSaveFile) {
        if (saveDirPaths == null) {
            return true;
        }

        for (int i = 0; i < saveDirPaths.length; i++) {
            File saveFile = new File(game.getSavePath() + File.separator + saveDirPaths[i]);

            if (saveFile.getName().equals(syncSaveFile.getName())) {
                if (saveFile.exists()) {
                    if (saveFile.lastModified() < syncSaveFile.lastModified()) {
                        return true;
                    } else {
                        return false;
                    }
                }

                return true;
            }
        }

        return true;
    }

    /**
     * Receives saves from the sync folder.
     */
    private void receiveSaves(GameInfo game) {
        File syncDir = new File(syncFolderPath + File.separatorChar + game.getName());
        File saveDir = new File(game.getSavePath());

        String[] saveDirPaths = saveDir.list();
        String[] syncDirPaths = syncDir.list();

        if (syncDirPaths != null) {
            for (int i = 0; i < syncDirPaths.length; i++) {
                File syncSaveFile = new File(syncDir.getAbsolutePath() + File.separator + syncDirPaths[i]);

                if (needsNewerFile(game, saveDirPaths, syncSaveFile)) {
                    if (!WindowsUtil.copyFileToDir(syncSaveFile, new File(game.getSavePath()))) {
                        throw new CrowSyncException("Could not copy file " + syncSaveFile.getAbsolutePath() + " to directory "
                                + game.getSavePath());
                    }

                    sendSystrayTask(new ReceivedTask(game.getName(), syncSaveFile.getAbsolutePath()));
                }
            }
        }
    }

    /**
     * Sends saves to the sync folder.
     */
    private void sendSaves(GameInfo game) {
        if (OSUtil.isWindows()) {
            Set<String> activeProcesses = WindowsUtil.listRunningProcesses();

            if (activeProcesses.contains((new File(game.getExecutablePath()).getName()))) {
                return;
            }
        }

        // first make a list of the current files and files in the sync folder
        File saveDir = new File(game.getSavePath());
        File syncDir = new File(syncFolderPath + File.separatorChar + game.getName());

        syncDir.mkdir();
        if (!syncDir.isDirectory()) {
            throw new CrowSyncException("Could not make directory " + saveDir.getAbsolutePath());
        }

        String[] saveDirPaths = saveDir.list();
        String[] syncDirPaths = syncDir.list();

        if (saveDirPaths == null) {
            return;
        }

        List<File> saveFiles = new LinkedList<File>();
        List<File> syncFiles = new LinkedList<File>();

        for (int i = 0; i < saveDirPaths.length; i++) {
            File file = new File(saveDir.getAbsolutePath() + File.separator + saveDirPaths[i]);
            saveFiles.add(file);
        }
        if (syncDirPaths != null) {
            for (int i = 0; i < syncDirPaths.length; i++) {
                File file = new File(syncDir.getAbsolutePath() + File.separator + syncDirPaths[i]);
                syncFiles.add(file);
            }
        }

        // sort by all save files date
        Collections.sort(saveFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        // remove all except a few
        int toDelete = saveFiles.size() - maxGameSaves;
        if (toDelete > 0) {
            for (int i = 0; i < toDelete; i++) {
                saveFiles.remove(0);
            }
        }

        List<File> toCopySaveFiles = new LinkedList<File>(saveFiles);
        // now remove those in this list that already exist
        Iterator<File> iter = toCopySaveFiles.iterator();
        while (iter.hasNext()) {
            File saveFile = iter.next();
            for (File syncFile : syncFiles) {
                if (saveFile.getName().equals(syncFile.getName())) {
                    if (saveFile.lastModified() >= syncFile.lastModified()) {
                        iter.remove();
                    }
                }
            }
        }

        // sync them now
        for (File saveFile : toCopySaveFiles) {
            if (!WindowsUtil.copyFileToDir(saveFile, syncDir)) {
                throw new CrowSyncException("Could not copy file " + saveFile.getAbsolutePath() + " to directory "
                        + syncDir.getAbsolutePath());
            }

            sendSystrayTask(new SentTask(game.getName(), saveFile.getAbsolutePath()));
        }

        // now remove files in the sync folder that we don't need any longer
        for (File syncFile : syncFiles) {
            boolean needsDeletion = true;

            for (File saveFile : saveFiles) {
                if (syncFile.getName().equals(saveFile.getName())) {
                    needsDeletion = false;
                }
            }

            if (needsDeletion) {
                syncFile.delete();
                sendSystrayTask(new RemovedTask(game.getName(), syncFile.getAbsolutePath()));
            }
        }

    }

    /**
     * Performs a syncing timestep. This will be 1 second plus additional time
     * required to perform filesystem tasks.
     * 
     * @throws InterruptedException
     * @returns Whether to keep running or not.
     */
    private boolean doTimestep() throws InterruptedException {
        final long MAX_WAIT = 4000;
        long beginTime = System.currentTimeMillis();

        for (long elapsed = 0; elapsed < MAX_WAIT;) {
            long remaining = MAX_WAIT - elapsed;
            SynchronizerTask task = syncQueue.poll(remaining, TimeUnit.MILLISECONDS);

            if (task != null) {
                if (!task.perform(this)) {
                    systrayQueue.put(new ExitTask());
                    return false;
                }
            }
            elapsed += (System.currentTimeMillis() - beginTime);
        }

        if (running) {
            for (GameInfo game : gameList) {
                // createSyncDir(game);
                receiveSaves(game);
                sendSaves(game);
            }
        }

        return true;
    }

    /**
     * The synchronizer thread will run as follows;
     * (not done outside CrowSync to implement the RELOAD event)
     * - Step 1: Take events during 5000 ms and execute associated actions
     * (thread will sleep most of the time)
     * - Step 2: Check selected filesystem path(s) for changes
     * - Step 3: Perform sync if necessary
     * - Step 4: goto Step 1
     * 
     * @throws InterruptedException
     */
    @Override
    public void run() {
        try {
            while (doTimestep()) {
            }
            systrayQueue.put(new ExitTask());
        } catch (final Exception e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog errorDialog = new ErrorDialog(true, e);
                    errorDialog.setVisible(true);
                }
            });
        }
    }

    /**
     * Sends an Task the the systray blocking queue
     */
    private void sendSystrayTask(SystrayTask task) {
        try {
            systrayQueue.put(task);
        } catch (InterruptedException e) {
            throw new CrowSyncException("Could not send task" + task.getClass().getName() + "; thread interrupted.", e);
        }
    }

    /**
     * Returns the systray's blocking queue
     * 
     * @return The systrayQueue.
     */
    public BlockingQueue<SystrayTask> getSystrayQueue() {
        return systrayQueue;
    }

    /**
     * Changes whether the synchronizer may synchronizer or not.
     * If changed to false it will not stop the thread, just syncing, New tasks
     * will still be able to be received.
     * 
     * @param running Whether to synchronize or not.
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Makes the synchronizer load or reload its cached configuration.
     * 
     * @param configFile The config file.
     * @param gameFile The games info file.
     */
    public void loadConfig(ConfigFile configFile, GameFile gameFile) {
        syncFolderPath = new String(configFile.getSyncFolderPath());
        maxGameSaves = configFile.getMaxSavesPerGame();
        gameList = new LinkedList<GameInfo>(gameFile.getGameList());
    }
}
