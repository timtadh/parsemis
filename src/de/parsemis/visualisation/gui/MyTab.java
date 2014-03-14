/**
 * Created on Feb 07, 2007
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.parsemis.MainFrame;
import de.parsemis.graph.HPGraph;
import de.parsemis.visualisation.GraphPanel;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class MyTab<NodeType, EdgeType> extends JPanel implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JPanel drawPane;

	JPanel image;

	HPGraph<NodeType, EdgeType> selectedGraph;

	MyScrollPane<NodeType, EdgeType> scrollPane;

	JScrollPane footerScrollPane = null;

	int tabIndex;

	// int visualisationType = 0;

	int selectedGraphIndex = 0;

	boolean setColor = true;

	boolean setCarbonLabels = false;

	boolean setHighQuality = true;

	boolean setSequence = true;

	private final PropertyChangeSupport mtListeners;

	private Properties props;

	private GraphPanel currentDemo = null;

	String[] exportFileFormats = { "png" };

	private JLabel top = null;

	private JLabel footerLabel = null;

	private JTextArea footer = null;

	private JPanel footerPanel = null;

	public MyTab(final int index, final Properties p) {
		super(new BorderLayout());
		props = p;
		tabIndex = index;
		this.setBorder(BorderFactory.createTitledBorder(new EmptyBorder(0, 0,
				0, 0)));

		addCenterPane();
		addTop();
		addFooter();
		mtListeners = new PropertyChangeSupport(this);
		scrollPane = new MyScrollPane<NodeType, EdgeType>(tabIndex);
		scrollPane.addPropertyChangeListener("fragment selected", this);
		scrollPane.addPropertyChangeListener("graph selected", this);
		this.addPropertyChangeListener("visualisation type", scrollPane);
		this.addPropertyChangeListener("remove old data", scrollPane);
		this.addPropertyChangeListener("colored", scrollPane);
		this.addPropertyChangeListener("carbon labels", scrollPane);
		this.addPropertyChangeListener("high quality", scrollPane);
		// this.mtListeners.firePropertyChange("visualization type", -1,
		// visualisationType);
		this.addPropertyChangeListener("remove all", scrollPane);

	}

	private void addCenterPane() {
		drawPane = new JPanel(new BorderLayout());
		drawPane.setBorder(new EmptyBorder(9, 0, 0, 0));
		image = new JPanel(new GridLayout());
		image.setSize(drawPane.getSize());
		drawPane.add(image, BorderLayout.CENTER);
		add(drawPane, BorderLayout.CENTER);
		initDrawPane();
	}

	private void addFooter() {

		footerPanel = new JPanel(new BorderLayout());
		footerPanel.setBorder(new EmptyBorder(5, 1, 0, 0));

		footer = new JTextArea();
		footerScrollPane = new JScrollPane(footer);
		initFooter();

		footerLabel = new JLabel();
		footerLabel.setHorizontalAlignment(SwingConstants.LEFT);

		footerPanel.add(footerLabel, BorderLayout.NORTH);
		footerPanel.add(footerScrollPane, BorderLayout.CENTER);
		footerPanel.setVisible(false);
		drawPane.add(footerPanel, BorderLayout.SOUTH);
	}

	public void addLeftColumn(final MyListModel<NodeType, EdgeType> listModel,
			final GraphPanel selectedFragment, final int index) {
		selectedGraphIndex = index + 1;
		scrollPane.setNewFragmentData(listModel, selectedFragment);
		add(scrollPane, BorderLayout.WEST);

		this.resetLanguage();
		this.initDrawPane();
		this.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final String str,
			final PropertyChangeListener l) {
		mtListeners.addPropertyChangeListener(str, l);
	}

	private void addTop() {
		top = new JLabel();
		top.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(top, BorderLayout.NORTH);
	}

	private void export(final File file) {
		System.err.println("Waiting for export...");
		int exportWidth, exportHeight;
		exportWidth = currentDemo.getComponent().getWidth();
		exportHeight = currentDemo.getComponent().getHeight();
		final Dimension dim = new Dimension(exportWidth, exportHeight);
		export(file, exportFileFormats, dim);

		// BufferedImage bimage = new BufferedImage(exportWidth, exportHeight,
		// BufferedImage.TYPE_INT_RGB);
		// switch (visualisationType) {
		// case 0:
		// ((Demo) this.currentDemo).paintOffscreen(bimage.createGraphics(),
		// dim);
		// break;
		// default:
		// ((PrefuseDemo)
		// this.currentDemo).paintOffscreen(bimage.createGraphics(), dim);
		// }
		// String fileName = file.getAbsolutePath();
		// String suffix = null;
		// for (int i = 0; i < exportFileFormats.length; i++) {
		// suffix = exportFileFormats[i];
		// // make export
		// try {
		// if (!fileName.endsWith("." + suffix)) {
		// fileName += "." + suffix;
		// file = new File(fileName);
		// }
		// ImageIO.write(bimage, suffix, file);
		// System.err.println("The export for " + file.getName()
		// + " is successful");
		// } catch (IllegalArgumentException e) {
		// System.err.println("export for " + file.getName() + " failed");
		// System.err.println(e);
		// } catch (IOException e) {
		// System.err.println("export for " + file.getName() + " failed");
		// System.err.println(e);
		// }
		// }
	}

	/**
	 * Diese Funktion speichert das Graph-Bild in eine Datei.
	 * 
	 * @param file
	 * @param suffixes
	 * @param d
	 */
	public void export(File file, final String[] suffixes, final Dimension d) {
		System.err.println("Waiting for export...");
		if (d == null) {
			this.export(file);
			return;
		}
		final BufferedImage bimage = new BufferedImage((int) d.getWidth(),
				(int) d.getHeight(), BufferedImage.TYPE_INT_RGB);
		try {
			final GraphPanel exportImage = currentDemo.clone();
			exportImage.getComponent().setVisible(false);
			exportImage.getComponent().setPreferredSize(d);
			exportImage.getComponent().setSize(d);
			exportImage.paintOffscreen(bimage.createGraphics(), d);
			// switch (visualisationType) {
			// case 0:
			// exportImage = (Demo) ((Demo) currentDemo).clone();
			// break;
			// case 1:
			// // bimage = ((PrefuseDemo) this.currentDemo).paintOffscreen(d);
			// ((PrefuseDemo)
			// this.currentDemo).paintOffscreen(bimage.createGraphics(), d);
			// }
		} catch (final CloneNotSupportedException e1) {
			System.err.println("MyTab::clone failed");
		}

		String fileName = file.getAbsolutePath();
		String suffix = null;
		for (int i = 0; i < suffixes.length; i++) {
			suffix = suffixes[i];
			// make export
			try {
				if (!fileName.endsWith("." + suffix)) {
					fileName += "." + suffix;
					file = new File(fileName);
				}
				ImageIO.write(bimage, suffix, file);
				System.err.println("The export for " + file.getName()
						+ " is successful");
			} catch (final IllegalArgumentException e) {
				System.err.println("export for " + file.getName() + " failed");
				System.err.println(e);
			} catch (final IOException e) {
				System.err.println("export for " + file.getName() + " failed");
				System.err.println(e);
			}
		}
	}

	private String formatSequence(final String input) {
		final int newline = input.indexOf("\n");
		if (newline >= 0) {
			return input.substring(newline, input.length()).trim();
		}
		return input;
	}

	private void initDrawPane() {
		selectedGraph = null;
		image.removeAll();
	}

	private void initFooter() {
		switch (0) {
		case 0:
			footer.setRows(2);
			footerScrollPane
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			footerScrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			break;
		default:
			footer.setRows(5);
			footerScrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			footerScrollPane
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		}
		footer.setEditable(false);
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		System.err.println(this.getClass().getName() + " property changed "
				+ propertyName);
		int newValue;
		if (propertyName.equals("fragment selected")) {
			newValue = ((Integer) event.getNewValue()).intValue();
			this.mtListeners.firePropertyChange("fragment selected", -1,
					newValue);
			return;
		}

		if (propertyName.equals("graph selected")) {
			newValue = ((Integer) event.getNewValue()).intValue();
			this.mtListeners.firePropertyChange("graph selected", -1, newValue);
			return;
		}

		if (propertyName.equals("remove all")) {
			this.mtListeners.firePropertyChange(event);
			this.setInitTab();
		}

		if (propertyName.equals("remove old data")) {
			this.mtListeners.firePropertyChange(event);
			this.setInitTab();
		}

		// if (propertyName.equals("visualisation type")) {
		// visualisationType = ((Integer) event.getNewValue()).intValue();
		// this.mtListeners.firePropertyChange("visualisation type", -1,
		// visualisationType);
		// initFooter();
		// }

		if (propertyName.equals("colored")) {
			setColor = ((Boolean) event.getNewValue()).booleanValue();
			this.mtListeners.firePropertyChange(event);
		}

		if (propertyName.equals("carbon labels")) {
			setCarbonLabels = ((Boolean) event.getNewValue()).booleanValue();
			this.mtListeners.firePropertyChange(event);
		}

		if (propertyName.equals("high quality")) {
			setHighQuality = ((Boolean) event.getNewValue()).booleanValue();
			this.mtListeners.firePropertyChange(event);
			return;
		}
		if (propertyName.equals("export" + tabIndex)) {
			final File saveToFile = (File) event.getNewValue();
			final Dimension s = ImageAccessory.getCurrentDimension();
			export(saveToFile, exportFileFormats, s);
			return;
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.mtListeners.firePropertyChange(event);
			this.resetLanguage();
			return;
		}

		if (propertyName.equals("set empty tab")) {
			setEmptyTab();
		}

		if (propertyName.equals("set sequence")) {
			setSequence = (Boolean) event.getNewValue();
			if (selectedGraph != null) {
				footerPanel.setVisible(setSequence);
			}
		}

	}

	private void resetLanguage() {
		switch (tabIndex) {
		case 0:
			if (scrollPane != null) {
				top.setText(String.format(props
						.getProperty("fragmentBorderTitle" + tabIndex),
						this.scrollPane.listSize));
			}
			break;
		case 1:
			if (scrollPane != null) {
				top.setText(String.format(props
						.getProperty("fragmentBorderTitle" + tabIndex),
						selectedGraphIndex)
						+ String.format(props.getProperty("fragmentTitle"
								+ tabIndex), this.scrollPane.listSize));
			}
			break;
		}
		if (selectedGraph != null) {
			footerLabel.setText(String.format(props.getProperty("footerText"),
					Integer.parseInt(selectedGraph.getName())));
		}
	}

	private void setEmptyTab() {
		setInitTab();
	}

	private void setInitTab() {
		if (scrollPane != null) {
			remove(scrollPane);
		}
		if (image != null) {
			image.removeAll();
		}

		footerPanel.setVisible(false);
		footer.setText("");

	}

	@SuppressWarnings("unchecked")
	public void showGraph(final HPGraph g, final String listIndex) {
		selectedGraph = g;
		image.removeAll();
		currentDemo = null;
		footer.setText("");
		currentDemo = GraphPanelGenerator.createPanel(image.getSize(), g
				.toGraph(), false, this);
		image.add(currentDemo.getComponent());
		footer.append(formatSequence(MainFrame.settings.parser.serialize(g
				.toGraph())));
		footerLabel.setText(String.format(props.getProperty("footerText"),
				listIndex));
		footerPanel.setVisible(this.setSequence);
		image.repaint();

	}

}
