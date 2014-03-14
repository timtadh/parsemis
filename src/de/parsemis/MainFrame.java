/**
 * created May 3, 2007
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
package de.parsemis;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.general.Fragment;
import de.parsemis.parsers.SmilesParser;
import de.parsemis.visualisation.chemicalVisualisation.ChemicalVisualisationSettings;
import de.parsemis.visualisation.gui.ControlTabbedPanel;
import de.parsemis.visualisation.gui.MyMenu;
import de.parsemis.visualisation.gui.MyMultiTabbedPanel;
import de.parsemis.visualisation.gui.MyTabbedPanel;
import de.parsemis.visualisation.gui.StatusPanel;
import de.parsemis.visualisation.prefuseVisualisation.PrefuseVisualisationSettings;

/**
 * This class is a small preliminary demo tool to demonstrate the functionality 
 * of the ParSeMiS package.
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
@SuppressWarnings("unchecked")
public class MainFrame extends Thread implements ActionListener,
		PropertyChangeListener {

	static public ArrayList<HPGraph> embGraphs;

	static Collection<Graph<?, ?>> parsedGraphs;

	static int graphsNumber = 0;

	static public Fragment[] ff;

	public static Settings settings = null;

	// default work directory
	public final static String defaultPath = System.getProperty("user.dir")
			// + File.separator + "branches" + File.separator + "main"
			+ File.separator + File.separator + "src" + File.separator + "de"
			+ File.separator + "parsemis" + File.separator + "visualisation"
			+ File.separator + "gui";

	public static String workDir = null;

	public static String iconsDir = File.separator + "icons" + File.separator;

	public static String languageDir = File.separator + "languages"
			+ File.separator;

	static final String[] options = new String[] { "--findPathsOnly=false",
			"--findTreesOnly=true", "--findPathsOnly=true" };

	/**
	 * start a new Demo
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.

		// set up native look and feel for SWING
		try {
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			// ignore exception
		}
		String path = null;
		if (args.length != 0) {
			final File iconsDir = new File(args[0] + MainFrame.iconsDir);
			final File languageDir = new File(args[0] + MainFrame.languageDir);
			if ((iconsDir.exists()) && (languageDir.exists())) {
				path = args[0];
			}
		}

		javax.swing.SwingUtilities.invokeLater(new MainFrame(path));
	}

	private JFrame frame = null;

	Properties props;

	MyTabbedPanel tabbedPane;

	private MyMultiTabbedPanel multiTabbedPane;

	public File selectedFile;

	private MyMenu menu = null;

	private ControlTabbedPanel controlTabbedPanel = null;

	PropertyChangeSupport mfListeners;

	int minimumFrequency = 1;

	boolean isClosed = true;

	int typeIndex = 0;

	int algoIndex = 0;

	ArrayList<HPGraph> ffHP;

	int parserID = 1;

	private StatusPanel statusPanel = null;

	boolean setHighQuality = false;

	private boolean setColor = false;

	private boolean setCarbonLabels = false;

	private JPanel mainPane = null;

	public MainFrame(final String path) {
		if (path == null) {
			workDir = defaultPath;
		} else {
			workDir = path;
		}
		props = new Properties();
		loadProperties("us.props");
		setHighQuality = PrefuseVisualisationSettings.setHighQuality;
		setColor = ChemicalVisualisationSettings.doColoredLabels;
		setCarbonLabels = ChemicalVisualisationSettings.setCarbonLabels;
		mfListeners = new PropertyChangeSupport(this);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		System.err.println(this.getClass().getName() + " action performed "
				+ command);
		if (command.equals(props.getProperty("stepStartButton"))) {
			mainPane.remove(tabbedPane);
			mainPane.remove(multiTabbedPane);
			mainPane.add(multiTabbedPane, BorderLayout.CENTER);
			final String[] set = new String[] {
					"--graphFile=" + selectedFile.getAbsolutePath(),
					"--minimumFrequency=" + minimumFrequency,
					options[typeIndex], "--connectedFragments=" + isClosed,
					"--distribution=visualisation", };
			final List<SearchLatticeNode> init = new ArrayList<SearchLatticeNode>();
			// try {
			final Settings settings = Settings.parse(set);
			settings.graphs = parsedGraphs;
			/* Collection<Fragment> expectedFragments = */settings.algorithm
					.initialize(settings.graphs, settings.factory, settings);
			for (final Iterator<SearchLatticeNode> it = settings.algorithm
					.initialNodes(); it.hasNext();) {
				init.add(it.next());
			}
			System.err.println(init.size() + " initial nodes");
			multiTabbedPane.ext = settings.algorithm.getExtender(0);
			// } catch (InstantiationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IllegalAccessException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (ClassNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			multiTabbedPane.addLayer(init, 0, null); // einfach wird das
			// erste Panel nicht
			// angezeigt :(
			multiTabbedPane.addLayer(init, 0, null);
		}
		if (command.equals(props.getProperty("startButton"))) {
			mainPane.remove(tabbedPane);
			mainPane.remove(multiTabbedPane);
			mainPane.add(tabbedPane, BorderLayout.CENTER);
			final Thread t = new Thread() {
				@Override
				public void run() {
					if (graphsNumber == 0) {
						return;
					}
					actionPerformed(new ActionEvent(this, 0, props
							.getProperty("running")));
					Collection<Fragment> fragments = null;

					// try {
					String[] set = new String[] {
							"--graphFile=" + selectedFile.getAbsolutePath(),
							"--minimumFrequency=" + minimumFrequency,
							options[typeIndex],
							"--connectedFragments=" + isClosed };
					Settings settings = Settings.parse(set);
					settings.graphs = parsedGraphs;

					// TODO set algorithmus type for the mining
					switch (algoIndex) {
					case 0:
						settings.algorithm = new de.parsemis.algorithms.gSpan.Algorithm();
						break;
					default:
						settings.algorithm = new de.parsemis.algorithms.gSpan.Algorithm();
					}

					fragments = Miner.mine(settings.graphs, settings);
					// } catch (InstantiationException e) {
					// System.err.println("Demo::actionPerformed:: "
					// + e.getMessage());
					// e.printStackTrace();
					// } catch (IllegalAccessException e) {
					// System.err.println("Demo::actionPerformed:: "
					// + e.getMessage());
					// e.printStackTrace();
					// } catch (ClassNotFoundException e) {
					// System.err.println("Demo::actionPerformed:: "
					// + e.getMessage());
					// e.printStackTrace();
					// }

					int size = fragments.size();
					for (Iterator it = fragments.iterator(); it.hasNext();) {
						final Fragment frag = (Fragment) it.next();
						if (frag.toGraph().getNodeCount() == 1) {
							size--;
						}
					}

					ff = new Fragment[size];
					ffHP = new ArrayList<HPGraph>();

					int i = -1;
					for (Iterator it = fragments.iterator(); it.hasNext();) {
						final Fragment frag = (Fragment) it.next();
						if (frag.toGraph().getNodeCount() > 1) {
							ff[++i] = frag;
							ffHP.add(frag.toGraph().toHPGraph());

						}
					}
					tabbedPane.setFragments(ffHP);
					if (ff.length == 0) {
						mfListeners.firePropertyChange("set empty tab", null,
								null);
					} else {
						mfListeners.firePropertyChange("add fragments", null,
								null);
					}
					actionPerformed(new ActionEvent(this, 0, "search done"));

				}
			};
			t.start();
		} else if (command.equals(props.getProperty("running"))) {
			mfListeners.firePropertyChange("running", null, null);
			mfListeners.firePropertyChange("disable", null, null);
			mfListeners.firePropertyChange("remove all", null, null);

		} else if (command.equals("search done")) {
			mfListeners.firePropertyChange(props.getProperty("search_done"),
					null, null);
			mfListeners.firePropertyChange("enable", null, null);
		}

	}

	public void addPropertyChangeListener(final PropertyChangeListener l) {
		mfListeners.addPropertyChangeListener(l);
	}

	private void addPropertyChangeListener(final String string,
			final PropertyChangeListener l) {
		mfListeners.addPropertyChangeListener(string, l);
	}

	private JComponent createPane() {
		final BorderLayout bl = new BorderLayout();
		mainPane = new JPanel(bl);
		mainPane.add(this.getTopPanel(), BorderLayout.NORTH);

		controlTabbedPanel = new ControlTabbedPanel(props);
		this.addPropertyChangeListener("parsed", controlTabbedPanel);
		this.addPropertyChangeListener("disable", controlTabbedPanel);
		this.addPropertyChangeListener("enable", controlTabbedPanel);
		this.addPropertyChangeListener("set language properties",
				controlTabbedPanel);
		this.addPropertyChangeListener(props.getProperty("running"),
				controlTabbedPanel);
		this.addPropertyChangeListener(props.getProperty("search_done"),
				controlTabbedPanel);
		controlTabbedPanel.addPropertyChangeListener(this);

		mainPane.add(controlTabbedPanel, BorderLayout.WEST);

		tabbedPane = new MyTabbedPanel(props);
		this.addPropertyChangeListener("colored", tabbedPane);
		this.addPropertyChangeListener("carbon labels", tabbedPane);
		this.addPropertyChangeListener("add fragments", tabbedPane);
		this.addPropertyChangeListener("set empty tab", tabbedPane);
		this.addPropertyChangeListener("remove all", tabbedPane);
		this.addPropertyChangeListener("visualisation type", tabbedPane);
		this.addPropertyChangeListener("high quality", tabbedPane);
		this.addPropertyChangeListener("export", tabbedPane);
		this.addPropertyChangeListener("set language properties", tabbedPane);
		this.addPropertyChangeListener("set graph view", tabbedPane);
		this.addPropertyChangeListener("set mining view", tabbedPane);
		this.addPropertyChangeListener("set sequence", tabbedPane);

		this.addPropertyChangeListener("remove options", menu);
		this.addPropertyChangeListener("visualisation type", menu);
		this.addPropertyChangeListener("set language properties", menu);
		this.addPropertyChangeListener("colored", menu);
		this.addPropertyChangeListener("carbon labels", menu);
		this.addPropertyChangeListener("high quality", menu);

		tabbedPane.addPropertyChangeListener("add save button", this);

		mainPane.add(tabbedPane, BorderLayout.CENTER);

		mfListeners
				.firePropertyChange("colored", !this.setColor, this.setColor);
		mfListeners.firePropertyChange("carbon labels", !this.setCarbonLabels,
				this.setCarbonLabels);
		mfListeners.firePropertyChange("high quality", !this.setHighQuality,
				this.setHighQuality);
		multiTabbedPane = new MyMultiTabbedPanel(props);
		this.addPropertyChangeListener("colored", multiTabbedPane);
		this.addPropertyChangeListener("carbon labels", multiTabbedPane);
		this.addPropertyChangeListener("add fragments", multiTabbedPane);
		this.addPropertyChangeListener("set empty tab", multiTabbedPane);
		this.addPropertyChangeListener("remove all", multiTabbedPane);
		this.addPropertyChangeListener("visualisation type", multiTabbedPane);
		this.addPropertyChangeListener("high quality", multiTabbedPane);
		this.addPropertyChangeListener("export", multiTabbedPane);
		this.addPropertyChangeListener("set language properties",
				multiTabbedPane);
		this.addPropertyChangeListener("set graph view", multiTabbedPane);
		this.addPropertyChangeListener("set mining view", multiTabbedPane);
		this.addPropertyChangeListener("set sequence", multiTabbedPane);

		multiTabbedPane.addPropertyChangeListener("add save button", this);

		return mainPane;
	}

	private JPanel getTopPanel() {
		final GridBagLayout gb = new GridBagLayout();
		final GridBagConstraints constraints = new GridBagConstraints();

		final JPanel topPanel = new JPanel(gb);
		menu = new MyMenu(props);
		menu.addPropertyChangeListener(this);
		this.addPropertyChangeListener("add save button", menu);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.5;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(menu, constraints);
		topPanel.add(menu);

		statusPanel = new StatusPanel(props);
		this.addPropertyChangeListener("set text", statusPanel);
		this.addPropertyChangeListener("set status", statusPanel);
		this.addPropertyChangeListener("set language properties", statusPanel);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.insets = new Insets(10, 5, 10, 0);
		constraints.anchor = GridBagConstraints.LAST_LINE_START;
		gb.setConstraints(statusPanel, constraints);
		topPanel.add(statusPanel);

		return topPanel;
	}

	private void loadProperties(final String propsName) {
		try {
			final FileInputStream io = new FileInputStream(workDir
					+ languageDir + propsName);
			props.load(io);
			io.close();
		} catch (final IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		System.err.println(this.getClass().getName() + " property changed "
				+ propertyName);
		if (propertyName == props.getProperty("menu1_item1")) {
			selectedFile = (File) event.getNewValue();
			graphsNumber = 0;
			mfListeners.firePropertyChange("set text", -1, 0);
			mfListeners.firePropertyChange("remove all", null, null);
			mfListeners.firePropertyChange("remove options", null, null);
			if (!selectedFile.exists()) {
				mfListeners.firePropertyChange("set text", -1, 0);
				return;
			}
			final Thread t = new Thread() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {

					String[] parseSet = new String[] {
							"--graphFile=" + selectedFile.getAbsolutePath(),
							"--minimumFrequency=1" };
					/* String selectedFileName = */selectedFile.getName();
					// try {
					settings = Settings.parse(parseSet);
					if (settings.parser instanceof SmilesParser) {
						parserID = 0;
					} else {
						parserID = 1;
					}
					mfListeners.firePropertyChange("visualisation type", -1,
							parserID);

					parsedGraphs = Miner.parseInput(settings);

					// } catch (InstantiationException e) {
					// mfListeners.firePropertyChange("set text", -1, 1);
					//
					// } catch (IllegalAccessException e) {
					//
					// mfListeners.firePropertyChange("set text", -1, 1);
					//
					// } catch (ClassNotFoundException e) {
					//
					// mfListeners.firePropertyChange("set text", -1, 1);
					//
					// } catch (RuntimeException e) {
					//
					// mfListeners.firePropertyChange("set text", -1, 1);
					// }
				}
			};
			t.start();
			try {
				t.join();
				// Finished
				if (parsedGraphs == null) {
					mfListeners.firePropertyChange("set text", -1, 1);
					return;
				}
				graphsNumber = parsedGraphs.size();
				final Object[] args = { selectedFile.getName(), graphsNumber };
				mfListeners.firePropertyChange("set status", 0, args);
				mfListeners.firePropertyChange("parsed", 0, graphsNumber);
			} catch (final InterruptedException e) {
				// Thread was interrupted
			}
			return;
		}
		if (propertyName.equals("export")) {
			@SuppressWarnings("unused")
			final File saveToFile = (File) event.getNewValue();
			mfListeners.firePropertyChange(event);
			return;
		}

		if (propertyName == props.getProperty("menu2_item1")) {
			boolean newValue = (Boolean) event.getNewValue();
			ChemicalVisualisationSettings.doColoredLabels = newValue;
			mfListeners.firePropertyChange("colored", !newValue, newValue);
			return;
		}

		if (propertyName == props.getProperty("menu2_item2")) {
			boolean newValue = (Boolean) event.getNewValue();
			ChemicalVisualisationSettings.setCarbonLabels = newValue;
			mfListeners
					.firePropertyChange("carbon labels", !newValue, newValue);
			return;
		}

		if (propertyName == props.getProperty("menu2_item3")) {
			boolean newValue = (Boolean) event.getNewValue();
			PrefuseVisualisationSettings.setHighQuality = newValue;
			mfListeners.firePropertyChange("high quality", !newValue, newValue);
			return;
		}

		if (propertyName == "minimumFrequency") {
			minimumFrequency = ((Integer) event.getNewValue()).intValue();
			return;
		}

		if (propertyName == "start") {
			actionPerformed(new ActionEvent(this, 0, props
					.getProperty("startButton")));
			return;
		}

		if (propertyName == "step start") {
			actionPerformed(new ActionEvent(this, 0, props
					.getProperty("stepStartButton")));
			return;
		}

		if (propertyName == "closed") {
			isClosed = (Boolean) event.getNewValue();
			return;
		}

		if (propertyName == "set sequence") {
			mfListeners.firePropertyChange(event);
			return;
		}

		if (propertyName == "typeBox") {
			typeIndex = ((Integer) event.getNewValue()).intValue();
			return;
		}

		if (propertyName == "algoBox") {
			algoIndex = ((Integer) event.getNewValue()).intValue();
			return;
		}

		if (propertyName == "add save button") {
			mfListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("language")) {
			final String fileName = (String) event.getNewValue() + ".props";
			this.loadProperties(fileName);
			mfListeners.firePropertyChange("set language properties", null,
					props);
			this.resetLanguage();
			return;
		}

		if (propertyName.equals("set mining view")) {
			this.mfListeners.firePropertyChange(event);
			return;
		}
		if (propertyName.equals("set graph view")) {
			this.mfListeners.firePropertyChange(event);
		}
	}

	private void resetLanguage() {
		frame.setTitle(props.getProperty("programm_title"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.close();
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		frame = new JFrame();
		resetLanguage();
		frame.setIconImage(new ImageIcon(workDir + iconsDir + "logo.gif")
				.getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(createPane(), BorderLayout.CENTER);
		frame.setSize(800, 600);

		// Display the window.
		frame.setVisible(true);
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

}
