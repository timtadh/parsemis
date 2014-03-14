/**
 * Created on Apr 20, 2007
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

import de.parsemis.MainFrame;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class MyMenu extends JMenuBar implements ActionListener,
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Properties props = null;

	private JMenu fileMenu = null;

	private JMenu optionMenu = null;

	private JMenu subMenu = null;

	private JMenuItem optionMenuItems[] = null;

	private JMenuItem fileMenuItems[] = null;

	PropertyChangeSupport mmListeners;

	private int visualisationType = 1;

	ImageAccessory ia;

	final JLabel languageLabel = new JLabel();

	boolean setHighQuality = true;

	private boolean setColor = false;

	private boolean setCarbonLabels = false;

	public MyMenu(final Properties p) {
		super();
		props = p;
		mmListeners = new PropertyChangeSupport(this);
		initFileMenu();
		initOptionMenu();
		initLanguages();
		resetLanguage();
	}

	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		if (command.equals(props.getProperty("menu1_item3"))) {
			System.exit(0);
		}

		if (command.equals(props.getProperty("menu1_item1"))) {
			final JFileChooser chooser = new JFileChooser();
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle(props.getProperty("open_dialog"));
			chooser
					.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			final String oldPath = getParsemisPath();
			if (oldPath != null) {
				try {
					final File oldDir = new File(oldPath);
					chooser.setCurrentDirectory(oldDir);
				} catch (final Exception e) {
					// again be very quiet
				}
			}
			final int option = chooser.showOpenDialog(new JFrame());
			if (option == JFileChooser.APPROVE_OPTION) {
				mmListeners.firePropertyChange(
						props.getProperty("menu1_item1"), null, chooser
								.getSelectedFile());

			}
			storeParsemisFilename(chooser.getSelectedFile().toString());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}

		if (command.equals(props.getProperty("menu1_item2"))) {

			final Thread t = new Thread() {
				@Override
				public void run() {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setMultiSelectionEnabled(false);
					chooser.setDialogTitle(props.getProperty("save_dialog"));
					chooser.setAccessory(ia);
					chooser.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					chooser.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".png")
									|| f.isDirectory();
						}

						@Override
						public String getDescription() {
							return "*.png";
						}
					});

					int option = chooser.showSaveDialog(new JFrame());
					if (option == JFileChooser.APPROVE_OPTION) {
						mmListeners.firePropertyChange("export", null, chooser
								.getSelectedFile());
					}
				}
			};
			t.start();
			return;
		}

		boolean isSelected;
		if (command.equals(props.getProperty("menu2_item1"))) {
			isSelected = optionMenuItems[0].isSelected();
			mmListeners.firePropertyChange(props.getProperty("menu2_item1"),
					!isSelected, isSelected);
			return;
		}

		if (command.equals(props.getProperty("menu2_item2"))) {
			isSelected = optionMenuItems[1].isSelected();
			mmListeners.firePropertyChange(props.getProperty("menu2_item2"),
					!isSelected, isSelected);
			return;
		}

		if (command.equals(props.getProperty("menu2_item3"))) {
			isSelected = optionMenuItems[2].isSelected();
			mmListeners.firePropertyChange(props.getProperty("menu2_item3"),
					!isSelected, isSelected);
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener l) {
		mmListeners.addPropertyChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final String string,
			final PropertyChangeListener l) {
		mmListeners.addPropertyChangeListener(string, l);
	}

	public String getParsemisPath() {
		// FIXME: ugly code follows ;-)
		try {
			String separator = System.getProperty("path.separator");
			if (separator.charAt(0) == ':') {
				separator = "/";
			}
			final String userHomeDir = System.getProperty("user.home")
					+ separator + ".parsemis_gui";
			final File InFile = new File(userHomeDir);
			final FileReader InWriter = new FileReader(InFile);
			final BufferedReader bufIn = new BufferedReader(InWriter);
			final String ret = bufIn.readLine();
			bufIn.close();
			final int index = ret.lastIndexOf(separator);
			return ret.substring(0, index);
		} catch (final FileNotFoundException e) {
			// be quiet, even in case of exceptions
		} catch (final IOException e) {
			// be quiet, even in case of exceptions
		}
		return null;
	}

	private void initFileMenu() {
		fileMenu = new JMenu(props.getProperty("menu1"));
		fileMenuItems = new JMenuItem[3];
		fileMenuItems[0] = new JMenuItem(new ImageIcon(MainFrame.workDir
				+ MainFrame.iconsDir + "open_file.gif"));
		fileMenuItems[0].addActionListener(this);
		fileMenu.add(fileMenuItems[0]);

		fileMenuItems[1] = new JMenuItem(new ImageIcon(MainFrame.workDir
				+ MainFrame.iconsDir + "save_file.gif"));
		fileMenuItems[1].addActionListener(this);
		fileMenu.add(fileMenuItems[1]);
		fileMenuItems[1].setVisible(false);

		fileMenu.addSeparator();

		fileMenuItems[2] = new JMenuItem();
		fileMenuItems[2].addActionListener(this);
		fileMenu.add(fileMenuItems[2]);
		add(fileMenu);

	}

	private void initLanguages() {
		final ImageIcon de_icon = new ImageIcon(MainFrame.workDir
				+ MainFrame.iconsDir + "de.gif");
		final ImageIcon us_icon = new ImageIcon(MainFrame.workDir
				+ MainFrame.iconsDir + "en.gif");
		languageLabel.setIcon(de_icon);
		languageLabel.addMouseListener(new MouseListener() {

			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() == 1) {
					if (languageLabel.getIcon() == de_icon) {
						languageLabel.setIcon(us_icon);
						mmListeners.firePropertyChange("language", null, "de");
						return;
					}
					if (languageLabel.getIcon() == us_icon) {
						languageLabel.setIcon(de_icon);
						mmListeners.firePropertyChange("language", null, "us");
						return;
					}

				}
			}

			public void mouseEntered(final MouseEvent arg0) {
			}

			public void mouseExited(final MouseEvent arg0) {
			}

			public void mousePressed(final MouseEvent arg0) {
			}

			public void mouseReleased(final MouseEvent arg0) {
			}

		});
		add(languageLabel);

	}

	private void initOptionMenu() {
		optionMenu = new JMenu();
		optionMenu.setVisible(false);

		optionMenuItems = new JMenuItem[4];
		// Colored check box
		optionMenuItems[0] = new JCheckBoxMenuItem();
		((JCheckBoxMenuItem) optionMenuItems[0]).setState(this.setColor);
		optionMenuItems[0].addActionListener(this);
		// Carbon labels check box
		optionMenuItems[1] = new JCheckBoxMenuItem();
		((JCheckBoxMenuItem) optionMenuItems[1]).setState(this.setCarbonLabels);
		optionMenuItems[1].addActionListener(this);
		// High Quality check box
		optionMenuItems[2] = new JCheckBoxMenuItem();
		((JCheckBoxMenuItem) optionMenuItems[2]).setState(this.setHighQuality);
		optionMenuItems[2].addActionListener(this);

		// save buttom
		optionMenuItems[3] = new JMenuItem(new ImageIcon(MainFrame.workDir
				+ MainFrame.iconsDir + "save_file.gif"));
		optionMenuItems[3].addActionListener(this);

		subMenu = new JMenu();
		final JMenuItem menuItem = new JMenuItem("RadialTreeLayout");
		subMenu.add(menuItem);
		optionMenu.add(subMenu);

		optionMenu.add(optionMenuItems[0]);
		optionMenu.add(optionMenuItems[1]);
		optionMenu.add(optionMenuItems[2]);
		optionMenu.add(optionMenuItems[3]);
		add(optionMenu);

		ia = new ImageAccessory(props);
		ia.addPropertyChangeListener(this);
		this.addPropertyChangeListener("set language properties", ia);
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName.equals("visualisation type")) {
			final int newValue = ((Integer) event.getNewValue()).intValue();
			visualisationType = newValue;
			resetOptionMenu();
			return;
		}
		if (propertyName.equals("remove options")) {
			optionMenu.setVisible(false);
			return;
		}
		if (propertyName.equals("add save button")) {
			if (!optionMenuItems[3].isVisible()) {
				optionMenuItems[3].setVisible(true);
			}
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			mmListeners.firePropertyChange(event);
			this.resetLanguage();
		}

		if (propertyName.equals("high quality")) {
			this.setHighQuality = (Boolean) event.getNewValue();
			((JCheckBoxMenuItem) optionMenuItems[2])
					.setState(this.setHighQuality);
		}

		if (propertyName.equals("carbon labels")) {
			this.setCarbonLabels = (Boolean) event.getNewValue();
		}

		if (propertyName.equals("colored")) {
			this.setColor = (Boolean) event.getNewValue();
		}

	}

	private void resetLanguage() {
		fileMenu.setText(props.getProperty("menu1"));
		fileMenuItems[0].setText(props.getProperty("menu1_item1"));
		fileMenuItems[1].setText(props.getProperty("menu1_item2"));
		fileMenuItems[2].setText(props.getProperty("menu1_item3"));

		optionMenu.setText(props.getProperty("menu2"));
		optionMenuItems[0].setText(props.getProperty("menu2_item1"));
		optionMenuItems[1].setText(props.getProperty("menu2_item2"));
		optionMenuItems[2].setText(props.getProperty("menu2_item3"));
		optionMenuItems[3].setText(props.getProperty("menu1_item2"));

		subMenu.setText(props.getProperty("menu2_submenu"));
	}

	private void resetOptionMenu() {
		optionMenu.setVisible(true);
		switch (visualisationType) {
		case 0:
			optionMenuItems[0].setVisible(true);
			optionMenuItems[1].setVisible(true);
			optionMenuItems[2].setVisible(false);
			optionMenuItems[3].setVisible(false);
			subMenu.setVisible(false);
			break;
		default:
			optionMenuItems[0].setVisible(false);
			optionMenuItems[1].setVisible(false);
			optionMenuItems[2].setVisible(true);
			optionMenuItems[3].setVisible(false);
			subMenu.setVisible(true);
		}

	}

	public void storeParsemisFilename(final String filename) {
		// FIXME: ugly code follows ;-)
		try {
			String separator = System.getProperty("path.separator");
			if (separator.charAt(0) == ':') {
				separator = "/";
			}
			final String userHomeDir = System.getProperty("user.home")
					+ separator + ".parsemis_gui";
			final File outFile = new File(userHomeDir);
			final FileWriter outWriter = new FileWriter(outFile);
			final BufferedWriter bufOut = new BufferedWriter(outWriter);
			bufOut.write(filename);
			bufOut.close();
		} catch (final Exception e) {
			// be quiet, even in case of exceptions
		}
	}

}
