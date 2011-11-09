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

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import nl.koziuk.crowsync.CrowSync;
import nl.koziuk.crowsync.CrowSyncException;
import nl.koziuk.crowsync.app.CrowSyncApp;
import nl.koziuk.crowsync.gui.TrayIcon;
import nl.koziuk.crowsync.gui.UIFont;
import nl.koziuk.crowsync.persist.ConfigFile;
import nl.koziuk.crowsync.persist.GameFile;
import nl.koziuk.crowsync.persist.LogFile;
import nl.koziuk.crowsync.sync.CrowSyncSynchronizer;
import nl.koziuk.crowsync.sync.SynchronizerMonitor;
import nl.koziuk.crowsync.sync.SynchronizerTask;
import nl.koziuk.crowsync.sync.SynchronizerTask.StartTask;
import nl.koziuk.crowsync.util.IconUtil;
import nl.koziuk.crowsync.util.OSUtil;

/**
 * Starts the CrowSync system tray. It is used to launch the CrowSyncApp and
 * sends/receives tasks to the synchronizer.
 * 
 * @author marcin
 */
public class CrowSyncSystray extends SynchronizerMonitor {

    private static final ImageIcon PROGRAM_ICON = IconUtil.imageIcon("trayicon.png");
    private static final ImageIcon START_ICON = IconUtil.imageIcon("16x16/start.png");
    private static final ImageIcon STOP_ICON = IconUtil.imageIcon("16x16/stop.png");
    private static final ImageIcon RESTART_ICON = IconUtil.imageIcon("16x16/restart.png");

    @SuppressWarnings("unused")
    private final String[] args;

    private final JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem startItem;
    private JMenuItem stopItem;
    private JMenuItem restartItem;
    private TrayIcon trayIcon = null;
    private SystemTray tray = null;

    private ConfigFile configFile;
    private LogFile logFile;
    private GameFile gameFile;

    private final List<Activity> log;

    // atomic because it can also be changed by the synchronization thread
    private final AtomicInteger status = new AtomicInteger();

    private final BlockingQueue<SystrayTask> systrayQueue = new LinkedBlockingQueue<SystrayTask>();
    private final BlockingQueue<SynchronizerTask> syncQueue = new LinkedBlockingQueue<SynchronizerTask>();

    private CrowSyncApp crowSyncApp = null;

    /**
     * Creates the system tray.
     * 
     * @param args
     */
    public CrowSyncSystray(String[] args) {
        this.args = args;

        try {
            configFile = new ConfigFile(CrowSync.CONFIG_FILENAME);
        } catch (IOException e) {
            throw new CrowSyncException("Could not read configuration file " + CrowSync.CONFIG_FILENAME, e);
        }

        try {
            logFile = new LogFile(CrowSync.LOG_FILENAME);
        } catch (IOException e) {
            throw new CrowSyncException("Could not read log file " + CrowSync.LOG_FILENAME, e);
        }

        try {
            gameFile = new GameFile(CrowSync.GAMES_FILENAME);
        } catch (Exception e) {
            throw new CrowSyncException("Could not read games file " + CrowSync.GAMES_FILENAME, e);
        }

        // this must be done here so that the system tray also benefits from a
        // better GUI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore
        }

        if (OSUtil.isWindowsVista() | OSUtil.isWindows7()) {
            UIFont.changeFont(new FontUIResource("Segoe UI", Font.PLAIN, 12));
        }

        setupSystemTray();

        final CrowSyncSystray systray = this;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                crowSyncApp = new CrowSyncApp(systray);
            }
        });

        log = Collections.synchronizedList(logFile.getLog());
        notifyLog();

        try {
            runCrowSync();
        } catch (InterruptedException e) {
            throw new CrowSyncException("Interrupted while running the system tray thread.", e);
        }

        try {
            logFile.setLog(log);
            logFile.save();
        } catch (IOException e) {
            throw new CrowSyncException("Could not save log file " + CrowSync.LOG_FILENAME, e);
        }

        try {
            configFile.save();
        } catch (IOException e) {
            throw new CrowSyncException("Could not save config file " + CrowSync.CONFIG_FILENAME, e);
        }

        try {
            gameFile.save();
        } catch (Exception e) {
            throw new CrowSyncException("Could not save games XML file " + CrowSync.GAMES_FILENAME, e);
        }
    }

    /**
     * Starts the synchronization thread and starts listening for new tasks.
     * 
     * @throws InterruptedException
     */
    private void runCrowSync() throws InterruptedException {
        // start the CrowSync thread
        Thread crowSync = new Thread(new CrowSyncSynchronizer(systrayQueue, syncQueue));
        crowSync.start();

        // hello
        syncQueue.put(new StartTask(configFile, gameFile));

        // check for tasks
        taskLoop();

        // wait for thread to finish and stop
        try {
            crowSync.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // dispose all running GUI threads
        crowSyncApp.getFrame().dispose();
        trayIcon.dispose();
        tray.remove(trayIcon);
    }

    /**
     * Performs the loop that listens for new tasks. Returning here means that
     * the whole application will close.
     */
    private void taskLoop() {
        while (true) {
            SystrayTask task;
            try {
                task = systrayQueue.take();
            } catch (InterruptedException e) {
                throw new CrowSyncException("System tray task loop interrupted.", e);
            }

            if (!task.perform(this)) {
                break;
            }
        }
    }

    /**
     * Shows a popup message near the notification area.
     * 
     * @param message The message
     */
    public void showMessage(String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(CrowSync.PROGRAM_NAME, message, java.awt.TrayIcon.MessageType.INFO);
        }
    }

    /**
     * Returns the currently loaded configuration file. Access to methods this
     * variable from other threads should be synchronized.
     * 
     * @return A reference to the configuration file.
     */
    public synchronized ConfigFile getConfig() {
        return configFile;
    }

    /**
     * Returns the currently loaded games file. Access to methods of this
     * variable from other threads should be synchronized.
     * 
     * @return A reference to the games file.
     */
    public synchronized GameFile getGameFile() {
        return gameFile;
    }

    /**
     * Changes the system tray's status.
     * 
     * @param status The status to set.
     */
    public void setStatus(final int status) {
        this.status.set(status);
        if (status == CrowSyncSynchronizer.RUNNING) {
            startItem.setVisible(false);
            restartItem.setVisible(true);
            stopItem.setVisible(true);
        } else if (status == CrowSyncSynchronizer.STOPPED) {
            startItem.setVisible(true);
            restartItem.setVisible(false);
            stopItem.setVisible(false);
        } else {
            startItem.setVisible(true);
            restartItem.setVisible(true);
            stopItem.setVisible(true);
        }

        // notify the app
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                crowSyncApp.setStatus(status);
            }
        });
    }

    /**
     * Returns the log. Access to this variable from other threads is to be
     * synchronized.
     * 
     * @return The log.
     */
    public List<Activity> getLog() {
        return log;
    }

    /**
     * Add an activity to the log.
     * 
     * @param activity The activity to add to the log.
     */
    public void addToLog(final Activity activity) {
        log.add(activity);
        notifyLog();

    }

    /**
     * Returns the synchronizer blockingqueue.
     * 
     * @return The synchronizer blockingqueue.
     */
    @Override
    public BlockingQueue<SynchronizerTask> getSyncQueue() {
        return syncQueue;
    }

    /**
     * Returns a reference to the systray.
     * 
     * @return The crowsync systray.
     */
    @Override
    protected CrowSyncSystray getSystray() {
        return this;
    }

    /**
     * Notifies the app of a log update
     */
    private void notifyLog() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                crowSyncApp.updateLog();
            }
        });
    }

    /**
     * Sets up the System Tray.
     */
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            throw new CrowSyncException("System tray is not supported.");
        }

        // make popup menu
        ActionListener launchListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        crowSyncApp.getFrame().setVisible(true);
                    }
                });
            }
        };

        // setup elements

        JMenuItem showItem = new JMenuItem("Show " + CrowSync.PROGRAM_NAME);
        Font currentFont = showItem.getFont();
        showItem.setFont(new Font(currentFont.getName(), Font.BOLD, currentFont.getSize()));
        showItem.setIcon(PROGRAM_ICON);
        showItem.addActionListener(launchListener);

        popupMenu.add(showItem);
        popupMenu.addSeparator();

        startItem = new JMenuItem("Start");
        startItem.setIcon(START_ICON);
        popupMenu.add(startItem);
        startItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                startAction();
            }
        });

        stopItem = new JMenuItem("Stop");
        stopItem.setIcon(STOP_ICON);
        popupMenu.add(stopItem);
        stopItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                stopAction();
            }
        });

        restartItem = new JMenuItem("Restart");
        restartItem.setIcon(RESTART_ICON);
        popupMenu.add(restartItem);
        restartItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                restartAction();
            }
        });

        popupMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                exitAction();
            }
        });

        popupMenu.add(exitItem);

        // setup systray icon

        tray = SystemTray.getSystemTray();

        trayIcon = new TrayIcon(PROGRAM_ICON.getImage());
        trayIcon.setJPopupMenu(popupMenu);
        trayIcon.setToolTip(CrowSync.PROGRAM_NAME);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(launchListener);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new CrowSyncException("Could not attach icon to system tray.", e);
        }
    }
}
