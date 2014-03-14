/**
 * Created on Jul 06, 2007
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.gui;

import java.awt.Dimension;

import javax.swing.JComponent;

import de.parsemis.chemical.Atom;
import de.parsemis.graph.Graph;
import de.parsemis.utils.GraphUtils;
import de.parsemis.visualisation.GraphPanel;
import de.parsemis.visualisation.SugiyamaDemo;
import de.parsemis.visualisation.chemicalVisualisation.Demo;
import de.parsemis.visualisation.prefuseVisualisation.PrefuseDemo;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class GraphPanelGenerator {

	public static <NodeType, EdgeType> GraphPanel createPanel(
			final Dimension d, final Graph<NodeType, EdgeType> g,
			final boolean isFragment) {
		return createPanel(d, g, isFragment, null);

	}

	@SuppressWarnings("unchecked")
	public static <NodeType, EdgeType> GraphPanel createPanel(
			final Dimension d, final Graph<NodeType, EdgeType> g,
			final boolean isFragment, final JComponent propertyChanger) {
		GraphPanel demo;
		if (g.getNode(0).getLabel() instanceof Atom) {
			demo = new Demo(g, d, isFragment);
		} else if (GraphUtils.isDAG(g)) {
			demo = new SugiyamaDemo(d, g, isFragment);
			((SugiyamaDemo) demo).zoomToFit();
		} else {
			demo = new PrefuseDemo(d, g, isFragment);
		}
		demo.addToPropertyChangeListener(propertyChanger);
		return demo;
	}

}
