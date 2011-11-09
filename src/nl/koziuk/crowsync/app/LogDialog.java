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
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import nl.koziuk.crowsync.systray.Activity;

public class LogDialog extends JDialog {

    private final String[] tableColumns;
    private Object[][] tableRows;

    /**
     * Create the dialog.
     */
    public LogDialog(JFrame parent, final List<Activity> log) {
        super(parent, true);

        tableColumns = new String[] { "", "Type", "Message", "Timestamp" };
        tableRows = new Object[][] {};

        synchronized (log) {
            if (!log.isEmpty()) {
                tableRows = new Object[log.size()][tableColumns.length];
                int i = log.size() - 1;
                for (Activity activity : log) {
                    tableRows[i] = new Object[] { activity.getImageIcon(), activity.getName(), activity.getDescription(),
                            activity.getFormattedDate("yyyy-MM-dd HH:mm:ss") };
                    i--;
                }
            }
        }

        initComponents();
    }

    /**
     * Action of the OK button.
     */
    private void okButtonAction() {
        dispose();
    }

    /**
     * Initializes and positions the components.
     */
    private void initComponents() {
        setBounds(100, 100, 563, 300);
        getContentPane().setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(tableRows, tableColumns) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Class getColumnClass(int column) {
                if (column == 0) {
                    return Icon.class;
                } else {
                    return super.getColumnClass(column);
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        {
            JTable table = new JTable(model);

            table.setRowHeight(20);
            table.getColumnModel().getColumn(0).setMaxWidth(20);
            table.getColumnModel().getColumn(2).setPreferredWidth(300);
            table.getColumnModel().getColumn(3).setPreferredWidth(118);

            JScrollPane scrollPane = new JScrollPane(table);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
        }

        {
            JPanel buttonPane = new JPanel();

            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            {
                JButton button = new JButton("OK");

                button.setPreferredSize(new Dimension(77, 24));
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        okButtonAction();
                    }
                });

                buttonPane.add(button);
            }
        }
    }

}
