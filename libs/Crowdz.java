/*
 * Copyright 2006 - 2013
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.algorithm.community;

import java.util.Dictionary;
import java.util.HashMap;

import org.graphstream.algorithm.measure.MobilityMeasure;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * This class implements a mobile community detection algorithm for VANETs <i>et al</i>.
 * 
 * author Agata Grzybek
 * 
 */
public class Crowdz extends EpidemicCommunityAlgorithm {

	/**
	 * Name of the marker that is used to store weight of links on the graph
	 * that this algorithm is applied to.
	 */
	protected String weightMarker = "weight";


	/**
	 * Name of the marker that is used to store weight of links on the graph
	 * that this algorithm is applied to.
	 */
	protected String speedMarker = "vehicleSpeed";


	/**
	 * Name of the marker that is used to store weight of links on the graph
	 * that this algorithm is applied to.
	 */
	protected String angleMarker = "vehicleAngle";

	
	public Crowdz() {
		super();
	}

	public Crowdz(Graph graph) {
		super(graph);
	}

	public Crowdz(Graph graph, String marker) {
		super(graph, marker);
	}

	/**
	 * Create a new algorithm instance, attached to the specified graph,
	 * using the specified marker to store the community attribute, and the
	 * specified weightMarker to retrieve the weight attribute of graph edges.
	 * 
	 * @param graph
	 *            graph to which the algorithm will be applied
	 * @param marker
	 *            community attribute marker
	 * @param weightMarker
	 *            edge weight marker
	 */
	public Crowdz(Graph graph, String marker, String weightMarker) {
		super(graph, marker);
		this.weightMarker = weightMarker;
	}


	/**
	 * Sets the parameters
	 * 
	 * @param speedMarker
	 *            
	 * @param angleMarker
	 *            
	 */
	public void setParameters(String speedMarker, String angleMarker) {
		this.speedMarker = speedMarker;
		this.angleMarker = angleMarker;
	}

	/**
	 * Sets the preference exponent and hop attenuation factor to the given
	 * values.
	 * 
	 * @param speedMarker
	 *            
	 * @param angleMarker
	 *            
	 * @param weightMarker
	 *            edge weight marker
	 */
	@Override
	public void setParameters(Dictionary<String, Object> params) {
		this.speedMarker = (String) params.get("speedMarker");
		this.angleMarker = (String) params.get("angleMarker");
		this.weightMarker = (String) params.get("weightMarker");
	}
	
	@Override
	public void computeNode(Node node) {
		/*
		 * Recall and update the node current community and previous score
		 */
		Object previousCommunity = node.getAttribute(marker);
		Double previousScore = (Double) node.getAttribute(marker + ".score");
		
		super.computeNode(node);

		/*
		 * Update the node label score
		 */

		// Handle first iteration // originate new community
		if (previousCommunity == null) {
			previousCommunity = node.getAttribute(marker);
			previousScore = (Double) node.getAttribute(marker + ".score");
		} 
		
		/*
		 * The node is the originator of the community and hasn't changed
		 * community at this iteration (or we are at the first simulation step):
		 * keep the maximum label score
		 */
		if ((node.getAttribute(marker).equals(previousCommunity))
				&& (previousScore.equals(1.0)))
			node.setAttribute(marker + ".score", 1.0);

		/*
		 * Otherwise search for the highest score amongst neighbors and reduce
		 * it by decreasing factor
		 */
		else {
			Double maxLabelScore = Double.NEGATIVE_INFINITY;
			for (Edge e : node.getEnteringEdgeSet()) {
				Node v = e.getOpposite(node);
				if (v.hasAttribute(marker)
						&& v.getAttribute(marker).equals(
								node.getAttribute(marker))) {
					if ((Double) v.getAttribute(marker + ".score") > maxLabelScore)
						maxLabelScore = (Double) v.getAttribute(marker
								+ ".score");
				}
			}
			/*
			 *  If node disconnected from its neighbors, it has already originated community (in super.computeNode(node);
			 *  The case when the node continues traveling without neighbors was handled earlier
			 */
			if (!maxLabelScore.equals(Double.NEGATIVE_INFINITY)) { 
				node.setAttribute(marker + ".score", maxLabelScore); 
			}

		}
	}

	/**
	 * Compute the scores for all relevant communities for the selected node.
	 * 
	 * @param u
	 *            The node for which the scores computation is performed
	 * @complexity O(DELTA) where DELTA is is the average node degree in the
	 *             network
	 */
	@Override
	protected void communityScores(Node u) {
		/*
		 * Reset the scores for each communities
		 */
		communityScores = new HashMap<Object, Double>();

		/*
		 * Iterate over the nodes that this node "hears"
		 */
		for (Edge e : u.getEnteringEdgeSet()) {
			Node v = e.getOpposite(u);

			/*
			 * Update the count for this community
			 */
			if (v.hasAttribute(marker)) {

				// Compute the neighbor node current score
				Double score = (Double) v.getAttribute(marker + ".score");
				Double mobilitySimilarity = MobilityMeasure.computeRelativeMobility(u, v, speedMarker, angleMarker);
				

				// Update the score of the according community
				if (communityScores.get(v.getAttribute(marker)) == null) {
					communityScores.put(v.getAttribute(marker), score * mobilitySimilarity);
				}
				else {
					communityScores.put(v.getAttribute(marker), communityScores.get(v.getAttribute(marker)) + (score * mobilitySimilarity));
				}
			}
		}
	}

	@Override
	protected void originateCommunity(Node node) {
		super.originateCommunity(node);

		// Correct the original community score for the Leung algorithm
		node.setAttribute(marker + ".score", 1.0);
	}
}
