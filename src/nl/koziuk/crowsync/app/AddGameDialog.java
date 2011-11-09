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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.koziuk.crowsync.util.OSUtil;

public class AddGameDialog extends JDialog {

    private boolean validated = false;

    private JTextField textGameName;
    private JTextField textSaveGamePath;
    private JTextField textGameExePath;

    /**
     * Create the dialog to make a new game info entry.
     * 
     * @wbp.parser.constructor
     */
    public AddGameDialog(JDialog parent) {
        super(parent, true);

        initComponents();
    }

    /**
     * Create the dialog to modify existing game info.
     */
    public AddGameDialog(JDialog parent, String gameName, String savePath, String exePath) {
        super(parent, true);

        initComponents();

        textGameName.setText(gameName);
        textSaveGamePath.setText(savePath);
        textGameExePath.setText(exePath);
    }

    /**
     * Returns the game row entry if available.
     * 
     * @return An array of Objects containing the row with game info, or null.
     */
    public Object[] getGameEntry() {
        if (validated) {
            Object[] result = new Object[3];

            result[0] = textGameName.getText();
            result[1] = textSaveGamePath.getText();
            result[2] = textGameExePath.getText();

            return result;
        } else {
            return null;
        }
    }

    /**
     * Validates the fields.
     * 
     */
    private boolean doValidate() {
        if (textGameName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Game name not entered.", "Game name not entered", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (textSaveGamePath.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Game save folder not chosen.", "Game save folder not chosen", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (textGameExePath.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Game executable location not chosen.", "Game executable location not chosen",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        validated = true;
        return true;
    }

    /**
     * OK button action.
     */
    private void okAction() {
        if (doValidate()) {
            dispose();
        }

    }

    /**
     * Cancel button action.
     */
    private void cancelAction() {
        dispose();
    }

    /**
     * Browse save path action.
     */
    private void browseSavePathAction() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textSaveGamePath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Browse exe path action.
     */
    private void browseExePathAction() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (OSUtil.isWindows()) {
            chooser.setFileFilter(new FileNameExtensionFilter("Executable File", "exe"));
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textGameExePath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Initializes and positions the components.
     */
    private void initComponents() {
        final JPanel contentPanel = new JPanel();

        setTitle("Game");
        setBounds(100, 100, 468, 179);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(null);

        {
            JLabel lblGameName = new JLabel("Game Name");

            lblGameName.setBounds(10, 11, 74, 14);

            contentPanel.add(lblGameName);
        }

        {
            textGameName = new JTextField();

            textGameName.setBounds(125, 8, 112, 20);
            textGameName.setColumns(10);

            contentPanel.add(textGameName);
        }

        {
            textGameExePath = new JTextField();
            textGameExePath.setEditable(false);
            textGameExePath.setBounds(125, 70, 229, 20);
            textGameExePath.setColumns(10);

            contentPanel.add(textGameExePath);
        }

        {
            JButton buttonBrowseSavePath = new JButton("Browse...");
            buttonBrowseSavePath.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    browseExePathAction();
                }
            });

            buttonBrowseSavePath.setPreferredSize(new Dimension(79, 24));
            buttonBrowseSavePath.setBounds(364, 68, 79, 24);

            contentPanel.add(buttonBrowseSavePath);
        }

        {
            JLabel lblExePath = new JLabel("Game Executable");

            lblExePath.setBounds(10, 73, 112, 14);

            contentPanel.add(lblExePath);
        }

        {
            JLabel lblSaveGamePath = new JLabel("Save Game Folder");

            lblSaveGamePath.setBounds(10, 42, 112, 14);

            contentPanel.add(lblSaveGamePath);
        }

        {
            textSaveGamePath = new JTextField();

            textSaveGamePath.setEditable(false);
            textSaveGamePath.setColumns(10);
            textSaveGamePath.setBounds(125, 39, 229, 20);

            contentPanel.add(textSaveGamePath);
        }

        {
            JButton buttonBrowseExePath = new JButton("Browse...");
            buttonBrowseExePath.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    browseSavePathAction();
                }
            });

            buttonBrowseExePath.setPreferredSize(new Dimension(79, 24));
            buttonBrowseExePath.setBounds(364, 37, 79, 24);

            contentPanel.add(buttonBrowseExePath);

            {
                JPanel buttonPane = new JPanel();
                buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
                getContentPane().add(buttonPane, BorderLayout.SOUTH);

                {
                    JButton okButton = new JButton("OK");
                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            okAction();
                        }
                    });

                    okButton.setPreferredSize(new Dimension(77, 24));
                    getRootPane().setDefaultButton(okButton);

                    buttonPane.add(okButton);
                }

                {
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cancelAction();
                        }
                    });

                    cancelButton.setPreferredSize(new Dimension(77, 24));

                    buttonPane.add(cancelButton);
                }
            }
        }
    }
}
