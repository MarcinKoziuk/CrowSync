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
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import nl.koziuk.crowsync.CrowSync;
import nl.koziuk.crowsync.CrowSyncException;
import nl.koziuk.crowsync.gui.ImagePanel;
import nl.koziuk.crowsync.util.IconUtil;
import nl.koziuk.crowsync.util.StringUtil;

/**
 * About Dialog
 * 
 * @author marcin
 */
public class AboutDialog extends JDialog implements HyperlinkListener {

    private final JPanel contentPanel = new JPanel();

    private final ImageIcon aboutIcon = IconUtil.imageIcon("about.png");

    private final String dialogTitle;
    private final String lblCrowText;
    private final String editorPaneInfoText;

    /**
     * Creates the about dialog.
     */
    public AboutDialog(JFrame parent) {
        super(parent, true);

        Font defaultFont = UIManager.getDefaults().getFont("Label.font");

        dialogTitle = "About " + CrowSync.PROGRAM_NAME;

        lblCrowText = "<html><div style='margin: 10px'>"
                + "<span style='font-size: 3em; font-family: \"segoe ui\", sans-serif'>"
                + CrowSync.PROGRAM_NAME
                + "</span><br />version "
                + CrowSync.VERSION +
                "</div>";

        editorPaneInfoText = "<html><head></head><body><div style='margin: 5px; font-size: 9px; font-family: \""
                + defaultFont.getFamily() + "\", sans-serif'>"
                + "<strong>Copyright © 2011 " + StringUtil.commaStringFromArray(CrowSync.AUTHORS) + ". All rights reserved.</strong><br />"
                + CrowSync.PROGRAM_DESCRIPTION + "</p>"
                + "<p>Licensed under the <a href='http://www.opensource.org/licenses/ISC/'>ISC license</a>. "
                + "See the readme file for additional information.<br /> Visit the <a href='" + CrowSync.HOMEPAGE
                + "'>project's homepage</a> or clone our <a href='" + CrowSync.GITHUB_HOMEPAGE + "'>GitHub repository</a>.<br />"
                + "For comments, support or questions contact <a href='" + CrowSync.SUPPORT_EMAIL + "'>"
                + CrowSync.SUPPORT_EMAIL + "</a>.</p></div></body></html>";

        initComponents();
    }

    /**
     * Action of the OK button.
     */
    private void okButtonAction() {
        dispose();
    }

    /**
     * Called when an URL is clicked on the interface.
     */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent arg0) {
        if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(arg0.getURL().toURI());
                } catch (IOException | URISyntaxException e) {
                    throw new CrowSyncException("Could not handle URL", e);
                }
            }

        }
    }

    /**
     * Initializes and positions the components.
     */
    private void initComponents() {
        setTitle(dialogTitle);
        setBounds(100, 100, 440, 323);
        setResizable(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        {
            ImageIcon image = aboutIcon;
            JPanel logoPanel = new ImagePanel(image.getImage());

            logoPanel.setBackground(Color.WHITE);

            getContentPane().add(logoPanel, BorderLayout.NORTH);
            logoPanel.setLayout(new BorderLayout(0, 0));

            {
                JLabel lblcrow = new JLabel(lblCrowText);

                logoPanel.add(lblcrow, BorderLayout.EAST);
            }
        }

        {
            JEditorPane editorPaneInfo = new JEditorPane("text/html", editorPaneInfoText);

            editorPaneInfo.setBackground(new Color(240, 240, 240));
            editorPaneInfo.setEditable(false);
            editorPaneInfo.addHyperlinkListener(this);

            contentPanel.add(editorPaneInfo);
        }

        {
            JPanel buttonPane = new JPanel();
            FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT, 10, 10);

            buttonPane.setLayout(fl_buttonPane);

            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            {
                JButton okButton = new JButton("OK");

                okButton.setPreferredSize(new Dimension(77, 24));
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        okButtonAction();
                    }
                });

                buttonPane.add(okButton);
            }
        }
    }
}
