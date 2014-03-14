/**
 * Created on Jun 15, 2005
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2005 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.chemicalVisualisation;

import java.util.HashSet;

/**
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Diese Klasse verwaltet Informationen ueber eine unverzweigte Kette
 * 
 */
public class Chain {

	private int beginOfChain;

	private int endOfChain;

	HashSet<Integer> chain;

	HashSet<Integer>[] groups;

	private int[] priority;

	/**
	 * a new Chain
	 */
	public Chain() {
		chain = new HashSet<Integer>();
		initGroups();
	}

	/**
	 * a new Chain
	 * 
	 * @param c
	 */
	public Chain(final Chain c) {
		chain = c.chain;
		groups = c.groups;
		beginOfChain = c.getBeginOfChain();
		endOfChain = c.getEndOfChain();
	}

	/**
	 * a new Chain
	 * 
	 * @param begin
	 */
	public Chain(final int begin) {
		beginOfChain = begin;
		// chain = new HashSet<Integer>();
		// chain.add(begin);
		chain = new HashSet<Integer>();
		initGroups();

	}

	/**
	 * Diese Funktion verknuepft zwei Ketten miteinander
	 * 
	 * @param nextChain
	 */
	public void addChainToChain(final Chain nextChain) {
		if (beginOfChain == -1) {
			beginOfChain = nextChain.getBeginOfChain();
		}
		endOfChain = nextChain.getEndOfChain();
		chain.addAll(nextChain.chain);
		addTwoGroups(nextChain.groups);
	}

	/**
	 * adds groups
	 * 
	 * @param data
	 */
	public void addGroups(final HashSet<Integer>[] data) {
		addTwoGroups(data);
	}

	/**
	 * adds a node to the chain
	 * 
	 * @param node
	 */
	public void addNodeToChain(final int node) {
		chain.add(node);
	}

	private void addTwoGroups(final HashSet<Integer>[] nextGroups) {
		for (int i = 0; i < groups.length; i++) {
			groups[i].addAll(nextGroups[i]);
		}
	}

	/**
	 * @param node
	 * @return <code>true</code>, if the chain contains the given node
	 */
	public boolean containsNode(final int node) {
		return chain.contains(node);
	}

	/**
	 * @param node
	 * @return <code>true</code>, if the chain contains the given node
	 */
	public boolean containsNode(final Integer node) {
		return chain.contains(node);
	}

	/**
	 * @return the begin of teh chain
	 */
	public int getBeginOfChain() {
		return beginOfChain;
	}

	/**
	 * @return the end of the chain
	 */
	public int getEndOfChain() {
		return endOfChain;
	}

	/**
	 * @return the groups
	 */
	public HashSet<Integer>[] getGroups() {
		return groups;
	}

	/**
	 * @return the priority
	 */
	public int[] getPriority() {
		priority = new int[7];
		priority[0] = this.getSize();

		for (int i = 13; i < groups.length; i++) {
			priority[1] += groups[i].size();
		}
		// anzahl von Zyklen dazu addieren
		priority[1] += groups[22].size();
		priority[1] += groups[23].size();
		priority[2] = groups[3].size() + groups[4].size();
		priority[3] = groups[1].size();
		priority[4] = groups[3].size();
		for (int i = 7; i < 18; i++) {
			if (i == 12) {
				continue;
			}
			priority[5] += groups[i].size();
		}
		priority[5] += groups[5].size();
		priority[6] += groups[1].size();

		return priority;
	}

	/**
	 * liefert die Laenge der Kette
	 * 
	 * @return the size of the chain
	 */
	public int getSize() {
		return chain.size();
	}

	/**
	 * @return a string representing the groups
	 */
	public String groupsToPrint() {
		String out = "";
		for (int i = 0; i < groups.length; i++) {
			out += groups[i].toString() + ", ";
		}
		return out;
	}

	/**
	 * initilaize the groups
	 */
	@SuppressWarnings("unchecked")
	public void initGroups() {
		groups = new HashSet[24];
		for (int i = 0; i < 24; i++) {
			groups[i] = new HashSet<Integer>();
		}
	}

	/**
	 * prints the priority to System.out
	 * 
	 */
	public void printPriority() {
		final int[] priority = getPriority();
		System.out.println("Priority");
		for (int i = 0; i < 6; i++) {
			System.out.print(priority[i] + " ");
		}
		System.out.println();
	}

	/**
	 * sets the end of the chain
	 * 
	 * @param begin
	 */
	public void setBeginOfChain(final int begin) {
		beginOfChain = begin;
	}

	/**
	 * sets the end of the chain
	 * 
	 * @param end
	 */
	public void setEndOfChain(final int end) {
		endOfChain = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Size:" + getSize() + ", Nodes:" + chain + ", Groups:"
				+ groupsToPrint();
	}

}