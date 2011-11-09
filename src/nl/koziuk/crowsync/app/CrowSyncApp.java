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
package nl.koziuk.crowsync.app;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import nl.koziuk.crowsync.CrowSync;
import nl.koziuk.crowsync.ErrorDialog;
import nl.koziuk.crowsync.persist.ConfigFile;
import nl.koziuk.crowsync.persist.GameFile;
import nl.koziuk.crowsync.sync.CrowSyncSynchronizer;
import nl.koziuk.crowsync.sync.SynchronizerMonitor;
import nl.koziuk.crowsync.sync.SynchronizerTask;
import nl.koziuk.crowsync.sync.SynchronizerTask.ExitTask;
import nl.koziuk.crowsync.systray.Activity;
import nl.koziuk.crowsync.systray.CrowSyncSystray;
import nl.koziuk.crowsync.util.IconUtil;

/**
 * The crow main user interface.
 * 
 * @author marcin
 */
public class CrowSyncApp extends SynchronizerMonitor {
    private static final String RUNNING_MESSAGE = CrowSync.PROGRAM_SYNC_NAME + " is running.";
    private static final String STOPPED_MESSAGE = CrowSync.PROGRAM_SYNC_NAME + " has stopped.";
    private static final String NOT_RESPONDING_MESSAGE = CrowSync.PROGRAM_SYNC_NAME + " is not responding.";

    private static final ImageIcon RUNNING_ICON = IconUtil.imageIcon("48x48/running.png");
    private static final ImageIcon STOPPED_ICON = IconUtil.imageIcon("48x48/stopped.png");
    private static final ImageIcon NOT_RESPONDING_ICON = IconUtil.imageIcon("48x48/notresponding.png");

    private static final ImageIcon LOG_ICON = IconUtil.imageIcon("16x16/log.png");

    private final JFrame frame = new JFrame();

    // NOTE: Access to the system tray's attributes should be synchronized.
    private final CrowSyncSystray crowSyncSystray;

    private final String mntmAboutText;

    private static final int MAX_RECENT_ACTIVITIES = 5;
    private final List<JLabel> lblsActivities = new ArrayList<JLabel>();

    private final JLabel lblStatus = new JLabel();
    private final JMenuItem mntmStart = new JMenuItem("Start");
    private final JMenuItem mntmRestart = new JMenuItem("Restart");
    private final JMenuItem mntmStop = new JMenuItem("Stop");

    /**
     * Creates the crow frame.
     * 
     * @param crowSyncSystray The system tray object.
     */
    public CrowSyncApp(final CrowSyncSystray crowSyncSystray) {
        this.crowSyncSystray = crowSyncSystray;

        // Sets up an exception handler so that problems will not get silently
        // unnoticed.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                ErrorDialog errorDialog = new ErrorDialog(false, e);
                errorDialog.setVisible(true);
            }
        });

        mntmAboutText = "About " + CrowSync.PROGRAM_NAME + "...";

        initComponents();

        setStatus(CrowSyncSynchronizer.NOT_RESPONDING);
    }

    /**
     * Refreshes the entire log and recent activities. Is called by AppTasks.
     */
    public void updateLog() {
        List<Activity> log = crowSyncSystray.getLog();

        synchronized (log) {
            for (int i = 0; i < MAX_RECENT_ACTIVITIES; i++) {
                int j = log.size() - i - 1;

                if (j < 0) {
                    break;
                }

                Activity activity = log.get(j);
                JLabel label = lblsActivities.get(i);

                label.setText(activity.getDescription());
                label.setIcon(activity.getImageIcon());
            }
        }
    }

    /**
     * Notifies the app about CrowSync status changes. Called by AppTasks.
     * 
     * @param status The new status.
     */
    public void setStatus(int status) {
        if (status == CrowSyncSynchronizer.RUNNING) {
            lblStatus.setText(RUNNING_MESSAGE);
            lblStatus.setIcon(RUNNING_ICON);
            mntmStart.setEnabled(false);
            mntmRestart.setEnabled(true);
            mntmStop.setEnabled(true);
        } else if (status == CrowSyncSynchronizer.STOPPED) {
            lblStatus.setText(STOPPED_MESSAGE);
            lblStatus.setIcon(STOPPED_ICON);
            mntmStart.setEnabled(true);
            mntmRestart.setEnabled(false);
            mntmStop.setEnabled(false);
        } else {
            lblStatus.setText(NOT_RESPONDING_MESSAGE);
            lblStatus.setIcon(NOT_RESPONDING_ICON);
            mntmStart.setEnabled(true);
            mntmRestart.setEnabled(true);
            mntmStop.setEnabled(true);
        }
    }

    /**
     * Returns the application's main JFrame
     * 
     * @return The JFrame.
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the synchronizer queue.
     * 
     * @return The synchronizer queue.
     */
    @Override
    protected BlockingQueue<SynchronizerTask> getSyncQueue() {
        return crowSyncSystray.getSyncQueue();
    }

    /**
     * Returns a reference to the crow systray.
     * 
     * @return The crow systray.
     */
    @Override
    protected CrowSyncSystray getSystray() {
        return crowSyncSystray;
    }

    /**
     * Action of the Exit menu item.
     */
    @Override
    protected void exitAction() {
        sendSyncTask(new ExitTask());
        frame.dispose();
    }

    /**
     * Action of the Log menu item.
     */
    private void logAction() {
        LogDialog logDialog = new LogDialog(frame, crowSyncSystray.getLog());
        logDialog.setVisible(true);
    }

    /**
     * Action of the Preferences menu item.
     */
    private void preferencesAction() {
        ConfigFile config = crowSyncSystray.getConfig();
        GameFile games = crowSyncSystray.getGameFile();

        // TODO: synchronize inside the prefsDialog for performance reasons
        synchronized (config) {
            PrefsDialog prefsDialog = new PrefsDialog(frame, config, games);
            prefsDialog.setVisible(true);
        }
    }

    /**
     * Action of the About menu item.
     */
    private void aboutAction() {
        AboutDialog aboutDialog = new AboutDialog(frame);
        aboutDialog.setVisible(true);
    }

    /**
     * Action of the Hide menu item.
     */
    private void hideAction() {
        frame.dispose();
    }

    /**
     * Initializes and positions the components.
     */
    private void initComponents() {

        frame.setTitle(CrowSync.PROGRAM_NAME);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, 412, 237);

        frame.setIconImage(PROGRAM_ICON.getImage());

        Font defaultFont = UIManager.getDefaults().getFont("Label.font");

        {
            JMenuBar menuBar = new JMenuBar();
            frame.setJMenuBar(menuBar);

            {
                JMenu mnFile = new JMenu(CrowSync.PROGRAM_NAME);
                menuBar.add(mnFile);

                mntmStart.setIcon(START_ICON);
                mnFile.add(mntmStart);
                mntmStart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        startAction();
                    }
                });

                mntmStop.setIcon(STOP_ICON);
                mnFile.add(mntmStop);
                mntmStop.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        stopAction();
                    }
                });

                mntmRestart.setIcon(RESTART_ICON);
                mnFile.add(mntmRestart);
                mntmRestart.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        restartAction();
                    }
                });

                mnFile.addSeparator();

                JMenuItem mntmPreferences = new JMenuItem("Preferences...");
                mntmPreferences.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        preferencesAction();
                    }
                });

                mnFile.add(mntmPreferences);

                JMenuItem mntmLog = new JMenuItem("Log...");
                mntmLog.setIcon(LOG_ICON);
                mnFile.add(mntmLog);
                mntmLog.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        logAction();
                    }
                });

                mnFile.addSeparator();

                JMenuItem mntmHide = new JMenuItem("Hide");
                mntmHide.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideAction();
                    }
                });

                mntmHide.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q,
                        java.awt.event.InputEvent.CTRL_MASK));
                mnFile.add(mntmHide);

                JMenuItem mntmExit = new JMenuItem("Exit");
                mntmExit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        exitAction();
                    }
                });

                mnFile.add(mntmExit);

            }

            {
                JMenu mnHelp = new JMenu("Help");
                menuBar.add(mnHelp);

                JMenuItem mntmAbout = new JMenuItem(mntmAboutText);
                mntmAbout.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        aboutAction();
                    }
                });

                mntmAbout.setIcon(PROGRAM_ICON);

                mnHelp.add(mntmAbout);
            }
        }

        {
            JPanel contentPane = new JPanel();

            contentPane.setBackground(UIManager.getColor("EditorPane.inactiveBackground"));
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            contentPane.setLayout(new BorderLayout(0, 0));

            frame.setContentPane(contentPane);

            JPanel statusPane = new JPanel();

            statusPane.setBackground(UIManager.getColor("EditorPane.inactiveBackground"));
            contentPane.add(statusPane);
            statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.Y_AXIS));
            statusPane.add(lblStatus);

            lblStatus.setFont(new Font(defaultFont.getFamily(), Font.PLAIN, 16));

            // so that we can see the label in the designer
            lblStatus.setIcon(NOT_RESPONDING_ICON);
            lblStatus.setText(NOT_RESPONDING_MESSAGE);

            JPanel activityPane = new JPanel();
            activityPane.setBackground(UIManager.getColor("EditorPane.inactiveBackground"));
            contentPane.add(activityPane, BorderLayout.SOUTH);
            activityPane.setLayout(new BoxLayout(activityPane, BoxLayout.Y_AXIS));

            for (int i = 0; i < MAX_RECENT_ACTIVITIES; i++) {
                int fontSize = (i == 0 ? 18 : 14);

                JLabel lblActivity = new JLabel(" ");
                lblActivity.setFont(new Font(defaultFont.getFamily(), Font.PLAIN, fontSize));
                if (i == 0) {
                    lblActivity.setText("No recent activities.");
                }

                activityPane.add(lblActivity);
                lblsActivities.add(lblActivity);
            }
        }
    }
}
