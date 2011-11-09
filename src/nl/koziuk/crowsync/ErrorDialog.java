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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import nl.koziuk.crowsync.util.ExceptionUtil;
import nl.koziuk.crowsync.util.IconUtil;

/**
 * An error dialog to show serious errors that are probably bugs.
 * 
 * @author marcin
 */
public class ErrorDialog extends JDialog {

    private static final Icon ERROR_ICON = IconUtil.imageIcon("48x48/stopped.png");

    private final JPanel contentPanel = new JPanel();

    private final String lblHasEncounteredText;
    private final String dtrpnProblemDescriptionText;
    private final boolean continueButtonVisibility;

    /**
     * Creates the dialog from a CrowException.
     * 
     * @param isFatal Whether the error is fatal.
     * @param exception The exception.
     */
    public ErrorDialog(boolean isFatal, CrowSyncException exception) {
        this(isFatal, exception.getWrappedException().getClass().getName()
                + "\n"
                + exception.getDescription()
                + "\n\nStack Trace:"
                + exception.getStackTraceString());
    }

    /**
     * Creates the dialog from an Exception.
     * 
     * @param isFatal Whether the error is fatal.
     * @param exception The exception.
     */
    public ErrorDialog(boolean isFatal, Throwable exception) {
        this(isFatal, exception.getClass().getName()
                + "\n\nStack Trace:"
                + ExceptionUtil.getStackTraceString(exception));
    }

    /**
     * Creates the dialog.
     * 
     * @param isFatal Whether the error is fatal.
     * @param description The description.
     * @param exception The exception.
     * @wbp.parser.constructor
     */
    private ErrorDialog(boolean isFatal, String description) {
        lblHasEncounteredText = "<html>"
                + CrowSync.PROGRAM_NAME
                + " has caught an unhandled exception"
                + (isFatal ? " and needs to close" : "")
                + ".\n ";

        dtrpnProblemDescriptionText = description;

        continueButtonVisibility = !isFatal;

        initComponents();
    }

    /**
     * Action of the Quit button.
     */
    private void quitAction() {
        System.exit(1);
    }

    /**
     * Action of the Continue button.
     */
    private void continueAction() {
        dispose();
    }

    /**
     * Initializes and positions the dialog's components.
     */
    private void initComponents() {
        setTitle("Error");
        setBounds(100, 100, 474, 275);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        {
            JPanel errorLabelPane = new JPanel();

            FlowLayout flowLayout = (FlowLayout) errorLabelPane.getLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);

            errorLabelPane.setPreferredSize(new Dimension(500, 55));
            errorLabelPane.setMinimumSize(new Dimension(500, 55));
            errorLabelPane.setMaximumSize(new Dimension(2147483647, 55));

            contentPanel.add(errorLabelPane);

            {
                JLabel lblIco = new JLabel();

                lblIco.setBounds(0, 0, 48, 48);
                lblIco.setIcon(ERROR_ICON);

                errorLabelPane.add(lblIco);
            }
            {
                JLabel lblHasEnountered = new JLabel(lblHasEncounteredText);

                lblHasEnountered.setBounds(58, 11, 401, 14);
                lblHasEnountered.setBackground(UIManager.getColor("EditorPane.disabledBackground"));

                errorLabelPane.add(lblHasEnountered);
            }
        }

        {
            JEditorPane dtrpnProblemDescription = new JEditorPane();

            dtrpnProblemDescription.setFont(new Font("Courier New", Font.PLAIN, 12));
            dtrpnProblemDescription.setContentType("text/plain");
            dtrpnProblemDescription.setBackground(UIManager.getColor("EditorPane.disabledBackground"));
            dtrpnProblemDescription.setText(dtrpnProblemDescriptionText);

            {
                JScrollPane scrollPane = new JScrollPane(dtrpnProblemDescription);

                scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                scrollPane.setMaximumSize(new Dimension(999, 600));
                scrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                dtrpnProblemDescription.setBounds(scrollPane.getBounds());

                contentPanel.add(scrollPane);
            }
        }

        {
            JPanel buttonPane = new JPanel();

            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            {
                JButton continueButton = new JButton("Continue");

                continueButton.setPreferredSize(new Dimension(85, 24));
                continueButton.setVisible(continueButtonVisibility);
                continueButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        continueAction();
                    }
                });

                getRootPane().setDefaultButton(continueButton);

                buttonPane.add(continueButton);
            }

            {
                JButton quitButton = new JButton("Quit");

                quitButton.setPreferredSize(new Dimension(77, 24));
                quitButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        quitAction();
                    }
                });

                getRootPane().setDefaultButton(quitButton);

                buttonPane.add(quitButton);
            }
        }
    }
}
