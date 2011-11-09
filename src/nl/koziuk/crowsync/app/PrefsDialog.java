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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import nl.koziuk.crowsync.persist.ConfigFile;
import nl.koziuk.crowsync.persist.GameFile;
import nl.koziuk.crowsync.persist.GameInfo;

public class PrefsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private final ConfigFile config;
	private final GameFile games;

	private JTextField textMaxGameSaves;
	private JTextField textSyncPath;
	private JTable table;
	private DefaultTableModel tableModel;
	private JButton btnApply;

	/**
	 * Create the dialog.
	 */
	public PrefsDialog(JFrame parent, ConfigFile config, GameFile games) {
		super(parent, true);

		this.config = config;
		this.games = games;

		initComponents();

		textMaxGameSaves.setText(Integer.toString(config.getMaxSavesPerGame()));
		textSyncPath.setText(config.getSyncFolderPath());

		List<GameInfo> gameList = games.getGameList();
		for (GameInfo game : gameList) {
			Object[] row = { game.getName(), game.getSavePath(),
					game.getExecutablePath() };
			tableModel.addRow(row);
		}
	}

	/**
	 * Checks the input and saves the configuration.
	 * 
	 * @return Whether saving completed successfully.
	 */
	private boolean save() {
		if (doValidate()) {
			config.setMaxSavesPerGame(Integer.parseInt(textMaxGameSaves
					.getText()));
			config.setSyncFolderPath(textSyncPath.getText());

			List<GameInfo> newGameList = new LinkedList<GameInfo>();
			for (int i = 0; i < table.getRowCount(); i++) {
				String name = (String) table.getValueAt(i, 0);
				String savePath = (String) table.getValueAt(i, 1);
				String exePath = (String) table.getValueAt(i, 2);

				newGameList.add(new GameInfo(name, savePath, exePath));
			}

			games.setGameList(newGameList);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Validate input fields.
	 * 
	 * @return Whether the validation was succeeded or not.
	 */
	private boolean doValidate() {
		int maxGameSaves = 0;

		try {
			maxGameSaves = Integer.parseInt(textMaxGameSaves.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Invalid number",
					"Invalid number", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (maxGameSaves < 0) {
			JOptionPane.showMessageDialog(this,
					"Number of game saves must be above zero.",
					"Number must be above zero", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (maxGameSaves > 20) {
			JOptionPane.showMessageDialog(this,
					"Synchronizing that many save files is not recommended.",
					"Quota warning.", JOptionPane.WARNING_MESSAGE);
		}

		return true;
	}

	/**
	 * Action of the Cancel button.
	 */
	private void cancelAction() {
		dispose();
	}

	/**
	 * Action of the Apply button.
	 */
	private void applyAction() {
		save();
		btnApply.setEnabled(false);
	}

	/**
	 * Action of the OK button.
	 */
	private void okAction() {
		if (save()) {
			dispose();
		}
	}

	/**
	 * Action of the New game button.
	 */
	private void newAction() {
		AddGameDialog dialog = new AddGameDialog(this);
		dialog.setVisible(true);

		Object[] newRow = dialog.getGameEntry();

		if (newRow != null) {
			tableModel.addRow(newRow);
			btnApply.setEnabled(true);
		}
	}

	/**
	 * Action of the Edit game button.
	 */
	private void editAction() {
		int selectedIdx = table.getSelectedRow();
		if (selectedIdx >= 0) {
			AddGameDialog dialog = new AddGameDialog(this, table.getValueAt(
					selectedIdx, 0).toString(), table
					.getValueAt(selectedIdx, 1).toString(), table.getValueAt(
					selectedIdx, 2).toString());
			dialog.setVisible(true);

			Object[] editedRow = dialog.getGameEntry();

			if (editedRow != null) {
				table.setValueAt(editedRow[0], selectedIdx, 0);
				table.setValueAt(editedRow[1], selectedIdx, 1);
				table.setValueAt(editedRow[2], selectedIdx, 2);
				btnApply.setEnabled(true);
			}
		}
	}

	/**
	 * Action of the Delete game button.
	 */
	private void deleteAction() {
		int selected = table.getSelectedRow();
		if (selected != -1) {
			tableModel.removeRow(selected);
			btnApply.setEnabled(true);
		}
	}

	/**
	 * Action of the Browse button.
	 */
	protected void browseAction() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			textSyncPath.setText(chooser.getSelectedFile().getAbsolutePath());
			btnApply.setEnabled(true);
		}
	}

	/**
	 * Action for when the max game saves textfield is clicked.
	 */
	private void textMaxGameSavesFocusAction() {
		btnApply.setEnabled(true);
	}

	/**
	 * Initializes and positions the components.
	 */
	private void initComponents() {
		setResizable(false);
		setBounds(100, 100, 450, 319);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

			contentPanel.add(tabbedPane);

			{
				JPanel globalTabPane = new JPanel();
				globalTabPane.setLayout(null);

				tabbedPane.addTab("Global", null, globalTabPane, null);

				{
					JPanel gameSavePane = new JPanel();

					gameSavePane.setBounds(0, 0, 419, 54);
					gameSavePane
							.setBorder(new TitledBorder(null, "Game Saves",
									TitledBorder.LEADING, TitledBorder.TOP,
									null, null));
					gameSavePane.setLayout(null);

					globalTabPane.add(gameSavePane);

					{
						JLabel lblMaxSaves = new JLabel(
								"Maximum number of save files per game");

						lblMaxSaves.setBounds(10, 24, 320, 19);

						gameSavePane.add(lblMaxSaves);
					}

					{
						textMaxGameSaves = new JTextField();
						textMaxGameSaves.addFocusListener(new FocusAdapter() {
							@Override
							public void focusGained(FocusEvent arg0) {
								textMaxGameSavesFocusAction();
							}
						});
						textMaxGameSaves.setText("5");
						textMaxGameSaves.setBounds(363, 21, 46, 20);

						gameSavePane.add(textMaxGameSaves);
					}
				}

				{
					JPanel syncFolderPane = new JPanel();

					syncFolderPane.setBounds(0, 59, 419, 126);
					syncFolderPane.setBorder(new TitledBorder(null,
							"Synchronization Folder", TitledBorder.LEADING,
							TitledBorder.TOP, null, null));
					syncFolderPane.setLayout(null);

					globalTabPane.add(syncFolderPane);

					{
						JLabel lblSyncFolder = new JLabel(
								"<html>Path where the synchronized game saves will be copied to. This must be inside your Dropbox, Ubuntu One, SugarSync or similar folder.");
						lblSyncFolder.setBounds(10, 23, 399, 42);

						syncFolderPane.add(lblSyncFolder);
					}

					{
						textSyncPath = new JTextField();
						textSyncPath.setEditable(false);
						textSyncPath.setBounds(10, 74, 310, 20);

						syncFolderPane.add(textSyncPath);
					}

					{
						JButton btnBrowse = new JButton("Browse...");
						btnBrowse.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								browseAction();
							}
						});

						btnBrowse.setPreferredSize(new Dimension(79, 24));
						btnBrowse.setBounds(330, 72, 79, 24);

						syncFolderPane.add(btnBrowse);
					}
				}
			}
			{
				JPanel gamesTabPane = new JPanel();
				tabbedPane.addTab("Games", null, gamesTabPane, null);
				gamesTabPane.setLayout(new BoxLayout(gamesTabPane,
						BoxLayout.X_AXIS));

				{
					table = new JTable();
					tableModel = new DefaultTableModel(

					new Object[][] {}, new String[] { "Name", "Save Path",
							"Executable Path" }) {
						@SuppressWarnings("rawtypes")
						Class[] columnTypes = new Class[] { Object.class,
								String.class, String.class };

						@SuppressWarnings({ "rawtypes", "unchecked" })
						@Override
						public Class getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}

						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					};

					table.setModel(tableModel);
					table.getColumnModel().getColumn(0).setPreferredWidth(107);
					table.getColumnModel().getColumn(1).setPreferredWidth(289);
					table.getColumnModel().getColumn(2).setPreferredWidth(151);

					JScrollPane scrollPane = new JScrollPane(table);

					gamesTabPane.add(scrollPane);
				}
				{
					JPanel editPane = new JPanel();

					editPane.setMinimumSize(new Dimension(90, 10));
					editPane.setPreferredSize(new Dimension(90, 10));

					gamesTabPane.add(editPane);
					{
						JButton btnNew = new JButton("New");
						btnNew.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								newAction();
							}
						});
						btnNew.setPreferredSize(new Dimension(77, 24));
						editPane.add(btnNew);
					}
					{
						JButton btnEdit = new JButton("Edit");
						btnEdit.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								editAction();
							}
						});
						btnEdit.setPreferredSize(new Dimension(77, 24));
						editPane.add(btnEdit);
					}
					{
						JButton btnDelete = new JButton("Delete");
						btnDelete.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								deleteAction();
							}
						});
						btnDelete.setPreferredSize(new Dimension(77, 24));
						editPane.add(btnDelete);
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();

			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);

			{
				JButton btnOk = new JButton("OK");

				btnOk.setPreferredSize(new Dimension(77, 24));
				btnOk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						okAction();
					}
				});

				buttonPane.add(btnOk);
			}

			{
				btnApply = new JButton("Apply");

				btnApply.setEnabled(false);
				btnApply.setPreferredSize(new Dimension(77, 24));
				btnApply.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						applyAction();
					}
				});

				buttonPane.add(btnApply);
			}

			{
				JButton btnCancel = new JButton("Cancel");

				btnCancel.setPreferredSize(new Dimension(77, 24));
				btnCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						cancelAction();
					}
				});

				buttonPane.add(btnCancel);
			}
		}
	}

}
