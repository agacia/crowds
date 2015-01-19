package org.graphstream.algorithm.community;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.math.stat.descriptive.moment.*;
import org.graphstream.graph.*;

public class MySandSharc_bak extends DecentralizedCommunityAlgorithm {

	protected HashMap<Object, Double> communityScores;
	protected HashMap<Object, Double> communityCounts;
	protected int stallingThreshold = 5;
	protected int breakPeriod = 5;

	public MySandSharc_bak() {
		super();
	}

	public MySandSharc_bak(Graph graph) {
		super(graph);
	}

	/**
	 * Allows to set generic parameters as a key,value 
	 * @param params
	 */
	@Override
	public void setParameters(Dictionary<String, Object> params) {
		super.setParameters(params);
		if (params.get("weightMarker") != null) {
			this.weightMarker = (String) params.get("weightMarker");
		}
		if (params.get("stallingThreshold") != null) {
			this.stallingThreshold = (Integer) params.get("stallingThreshold");
		}
		if (params.get("breakPeriod") != null) {
			this.breakPeriod = (Integer) params.get("breakPeriod");
		}		
	}

	/**
	 * Compute the node new assignment using the Sand-SHARC algorithm
	 * 
	 * @param u
	 *            Node for which the computation is performed
	 * @complexity O(DELTA^2) where DELTA is the average node degree in the
	 *             network
	 */
	@Override
	public void computeNode(Node u) {

		/*
		 * Recall previous community (will be used for originator update)
		 */
		Object previousCommunity = u.getAttribute(marker);
		Double previousScore = (Double) u.getAttribute(marker + ".score");
		u.setAttribute(marker + ".old_score", previousScore);

		if (u.getDegree() == 0 || previousCommunity == null) { // Revert to self-community if no more neighbors or manage first  iteration of the simulation
			originateCommunity(u);
			if (u.getDegree() == 0 ) {
				System.err.println("Originate degree0\t" + graph.getStep() + "\tnode\t" + u.getId() + "\tcom\t" + u.getAttribute(marker) + "\tprevious com\t" + previousCommunity + "\tdegree\t" + u.getDegree());
			}
		}
//		else if (u.hasAttribute(marker + ".break")) {
//			performBreakMode(u);
//		}
		else { // Still no update, perform standard SHARC assignment
			SawSharcComputeNode(u);
			/*
			 * If the node final score is 0, i.e. there were no preferred community,
			 * fall back to the "simple" epidemic assignment
			 */
			if (previousCommunity != null && ((Double) u.getAttribute(marker + ".score")) == 0.0) {
				//System.out.println(u.getId() + " Falling back to epidemic.");
				communityScores = communityCounts;
				epidemicComputeNode(u);
			}
		}
		
		updateOriginator(u, previousCommunity);

//		checkBreakMode(u, previousCommunity);
		
		
	}

	private void checkBreakMode(Node u, Object previousCommunity) {
		/*
		 * Update freshness counter and stalling value or reset everything if
		 * the node has changed community
		 */
		if (previousCommunity == null || previousCommunity.equals(u.getAttribute(marker))) {
			int freshness;
			if (u.hasAttribute(marker + ".freshness")) {
				freshness = (Integer) u.getAttribute(marker + ".freshness");
			} else {
				freshness = 0;
			}

			updateFreshessCounter(u);

			/*
			 * Has freshness be incremented ? If no, increment the
			 */
			if (freshness >= u.getNumber(marker + ".freshness")) {
				if (u.hasAttribute(marker + ".stalling")) {
					u.setAttribute(marker + ".stalling",
							u.getNumber(marker + ".stalling") + 1);
				} else {
					u.setAttribute(marker + ".stalling", 1);
				}
			} else
				u.setAttribute(marker + ".stalling", 0);

		} else {
			u.setAttribute(marker + ".freshness", 0);
			u.setAttribute(marker + ".stalling", 0);
		}

		if (u.hasAttribute(marker + ".stalling") && u.getNumber(marker + ".stalling") > 0) {
			/*
			 * Enable break mode if the stalling threshold is reached
			 */
			if (u.getNumber(marker + ".stalling") >= stallingThreshold) {
				// Enable break mode
				u.setAttribute(marker + ".break", breakPeriod - 1);
				u.setAttribute(marker + ".broken_community", u.getAttribute(marker));
//				System.err.println("Enter_break_mode\t" + graph.getStep() + "\tnode\t" + u.getId() + "\tcom\t" + u.getAttribute(marker) + "\tprevious com\t" + previousCommunity);
			}
		}
	}
	
	private void performBreakMode(Node u) {
		int remaining = (Integer) u.getAttribute(marker + ".break");

		if (!u.hasAttribute(marker + ".break_done")) {
			/*
			 * Search for a neighbor in break mode, otherwise, initiate a
			 * new community
			 */
			Object newCommunity = null;
			for (Edge e : u.getEnteringEdgeSet()) {
				Node v = e.getOpposite(u);
				if (v.hasAttribute(marker)
						&& v.hasAttribute(marker + ".break")
						&& v.hasAttribute(marker + ".break_done")
						&& v.hasAttribute(marker + ".broken_community")
						&& v.<Object> getAttribute(marker + ".broken_community").equals(
								u.<Object> getAttribute(marker+ ".broken_community"))) {
					newCommunity = v.getAttribute(marker);
				}
			}
			if (newCommunity == null) {
				Object com = u.getAttribute(marker);
				originateCommunity(u);
				System.err.println("In_break_mode_originate\t" + graph.getStep() + "\tnode\t" + u.getId() + "\tcom\t" + u.getAttribute(marker) + "\tprevious com\t" + com + "\tdegree\t" + u.getDegree());
			} else {
//				System.err.println("In_break_mode_join\t" + graph.getStep() + "\tnode\t" + u.getId() + "\tcom\t" + u.getAttribute(marker) + "\tprevious com\t" + newCommunity);
				u.setAttribute(marker, newCommunity);
			}
			u.setAttribute(marker + ".break_done", true);
		}

		/*
		 * Decrease break mode lifetime
		 */
		if (remaining > 0) {
			u.setAttribute(marker + ".break", remaining - 1);
		}

		/*
		 * Terminate break mode on lifetime expiration
		 */
		else if (remaining == 0) {
			u.removeAttribute(marker + ".break");
			u.removeAttribute(marker + ".broken_community");
			u.removeAttribute(marker + ".break_done");
		}
	}
	
	private void SawSharcComputeNode(Node u) {
		/*
		 * First construct the cdf based on link weights
		 */
		setMaxWeight(u);

		/*
		 * Calculate community scores
		 */
		communityScores(u);

		epidemicComputeNode(u);
	}
	
	private void epidemicComputeNode(Node u) {
		/*
		 * Search for the community with the highest score
		 */
		Object maxCommunity = null;
		Double maxScore = Double.NEGATIVE_INFINITY;

		TreeMap<Object, Double> scores = new TreeMap<Object, Double>(communityScores);
		for (Object c : scores.keySet()) {
			Double s = communityScores.get(c);

			if (s > maxScore || (s == maxScore && rng.nextDouble() >= 0.5)) {
				maxCommunity = c;
				maxScore = s;
			}
		}

		/*
		 * Update the node community
		 */
		if (maxCommunity == null)
			originateCommunity(u);
		else {
			u.setAttribute(marker, maxCommunity);
			u.setAttribute(marker + ".score", maxScore);
		}
	}
	
	@Override
	protected void originateCommunity(Node u) {
		super.originateCommunity(u);
		u.setAttribute(marker + ".score", 0.0);
		u.setAttribute(marker + ".originator", true);
		u.setAttribute(marker + ".new_originator", true);
	}

	protected void updateFreshessCounter(Node u) {
		/*
		 * Initialize freshness counter
		 */
		int freshness = 0;
		if (u.hasAttribute(marker + ".freshness"))
			freshness = (Integer) u.getAttribute(marker + ".freshness");

		/*
		 * Update the edge validity threshold for the current node
		 */
		setEdgeThreshold(u);

		/*
		 * Set the freshness counter to the highest value heard from one of the
		 * neighbors of the same community
		 */
		for (Edge e : u.getEnteringEdgeSet()) {
			Node v = e.getOpposite(u);
			if (v.hasAttribute(marker)
					&& v.hasAttribute(marker + ".freshness")
					&& v.<Object> getAttribute(marker).equals(
							u.<Object> getAttribute(marker))) {
				if (similarity(u, v) >= u.getNumber(marker + ".threshold")
						&& v.getNumber(marker + ".freshness") > freshness) {
					freshness = (int) v.getNumber(marker + ".freshness");
				}
			}
		}

		/*
		 * If the node is originator, increase this count
		 */
		if (u.hasAttribute(marker + ".originator"))
			freshness++;

		/*
		 * Update the freshness attribute
		 */
		u.setAttribute(marker + ".freshness", freshness);
	}

	/*
	 * Set the originator: Currently originator, pass the role to a neighbor
	 * node with higher score than me or to the neighbor with the highest
	 * score if i changed community
	 */
	protected void SawSharcUpdateOriginator(Node u, Object previousCommunity) {
		if (u.hasAttribute(marker + ".originator")) {
			Double score;
			if (previousCommunity == null
					|| previousCommunity.equals(u.getAttribute(marker)))
				score = (Double) u.getAttribute(marker + ".score");
			else {
				/*
				 * Originator node changed community
				 */
				score = Double.MIN_VALUE;

			}

			Node originator = u;
			for (Edge e : u.getEnteringEdgeSet()) {
				Node v = e.getOpposite(u);
				if (v.hasAttribute(marker)
						&& v.<Object> getAttribute(marker).equals(
								previousCommunity)
						&& v.hasAttribute(marker + ".score")
						&& (Double) v.getAttribute(marker + ".score") > score) {
					score = (Double) v.getAttribute(marker + ".score");
					originator = v;
				}
			}

			/*
			 * Update originator if necessary
			 */
			if (originator != u) {
				u.removeAttribute(marker + ".originator");
				originator.setAttribute(marker + ".originator", true);
				originator.setAttribute(marker + ".new_originator", true);

			}

			if (u.hasAttribute(marker + ".new_originator"))
				u.removeAttribute(marker + ".new_originator");
		}

	}
	
	protected void updateOriginator(Node u, Object previousCommunity) {
		/*
		 * Current node has originator token
		 */
		if (u.hasAttribute(marker + ".originator")
				&& !u.hasAttribute(marker + ".new_originator")) {

			/*
			 * Originator stayed in the same community: Make the originator
			 * token wander using a "local optimum favored" weighted random
			 * walk.
			 */
			if (previousCommunity != null
					&& previousCommunity.equals(u.getAttribute(marker))) {

				double score = u.getNumber(marker + ".score");
				double max = Double.NEGATIVE_INFINITY;
				HashMap<Node, Double> scores = new HashMap<Node, Double>();
				double total = 0;

				/*
				 * Search for the maximum neighboring score in the same
				 * community update total at the same time
				 */
				for (Edge e : u.getEnteringEdgeSet()) {
					Node v = e.getOpposite(u);
					if (v.hasAttribute(marker)
							&& v.<Object> getAttribute(marker).equals(
									u.<Object> getAttribute(marker))
							&& v.getId() != u.getAttribute(marker + ".originator_from")) {
						scores.put(v, v.getNumber(marker + ".score"));
						total += v.getNumber(marker + ".score");
						if (v.getNumber(marker + ".score") > max)
							max = v.getNumber(marker + ".score");
					}
				}

				/*
				 * Current node is the local optimum: Originator token will pass
				 * only with a given probability. Otherwise token is passed
				 * using weighted random walk
				 */
				if (max > score || rng.nextDouble() < (max / score)) {

					double random = rng.nextDouble() * total;
					Node originator = null;
					for (Node v : scores.keySet()) {
						if (random <= scores.get(v) &&
							v.getId() != u.getAttribute(marker + ".originator_from")) {
								originator = v;
						} else {
							random -= scores.get(v);
						}
					}

					if (originator != null) {
						u.removeAttribute(marker + ".originator");
						u.removeAttribute(marker + ".originator_from");
						
						originator.setAttribute(marker + ".originator", true);
						originator.setAttribute(marker + ".new_originator",
								true);
						originator.setAttribute(marker + ".originator_from", u.getId());
					}
				}
			}

			/*
			 * Originator node changed community: Simply pass the originator
			 * token to the neighbor of previous community with the highest
			 * score
			 */
			else {
				u.removeAttribute(marker + ".originator");
				u.removeAttribute(marker + ".originator_from");

				double score = Double.NEGATIVE_INFINITY;
				Node originator = null;
				for (Edge e : u.getEnteringEdgeSet()) {
					Node v = e.getOpposite(u);
					if (v.hasAttribute(marker)
							&& v.<Object> getAttribute(marker).equals(
									previousCommunity)
							&& v.hasAttribute(marker + ".score")
							&& v.getNumber(marker + ".score") > score) {
						score = v.getNumber(marker + ".score");
						originator = v;
					}
				}

				/*
				 * A neighbor is found
				 */
				if (originator != null) {
					originator.setAttribute(marker + ".originator", true);
					originator.setAttribute(marker + ".new_originator", true);
				}
			}
		}

		/*
		 * The node has been processed, so it can't be a new originator
		 */
		if (u.hasAttribute(marker + ".new_originator"))
			u.removeAttribute(marker + ".new_originator");
	}

	protected void setEdgeThreshold(Node u) {
		/*
		 * Mean and standard deviation
		 */
		Mean mean = new Mean();
		StandardDeviation stdev = new StandardDeviation();
		for (Edge e : u.getEnteringEdgeSet()) {
			Node v = e.getOpposite(u);
			if (v.hasAttribute(marker)
					&& v.<Object> getAttribute(marker) == u
							.<Object> getAttribute(marker)) {
				mean.increment(similarity(u, v));
				stdev.increment(similarity(u, v));
			}
		}

		/*
		 * Only consider as valid edges for which the similarity is above (mean
		 * - 0.5 * stdev)
		 */
		double threshold = mean.getResult() - 0.5 * stdev.getResult();
		u.setAttribute(marker + ".threshold", threshold);
	}
	
	/**
	 * NewSawSharc
	 */
	
	/**
	 * Name of the marker that is used to store weight of links on the graph
	 * that this algorithm is applied to.
	 */
	protected String weightMarker = "weight";

	/**
	 * Maximum weight on all incoming links
	 */
	protected Double maxWeight = Double.NEGATIVE_INFINITY;


	/**
	 * Neighborhood weighted similarity between two nodes.
	 * 
	 * @param a
	 *            The first node
	 * @param b
	 *            The second node
	 * @return The similarity value between the two nodes
	 * @complexity O(DELTA) where DELTA is the average node degree in the
	 *             network
	 */
	protected Double similarity(Node a, Node b) {
		Double sim;

		if (maxWeight == Double.NEGATIVE_INFINITY || maxWeight == 0.0)
			sim = neighborhood_similarity(a, b);
		else
			sim = neighborhood_similarity(a, b)
					* (getWeightInLinkFrom(a, b) / maxWeight);

		return sim;
	}

	protected void setMaxWeight(Node u) {

		maxWeight = Double.NEGATIVE_INFINITY;
		for (Edge e : u.getEnteringEdgeSet()) {
			Double weight = getWeightInLinkFrom(u, e.getOpposite(u));
			if (weight > maxWeight) {
				maxWeight = weight;
			}
		}
	}

	protected Double getWeightInLinkFrom(Node a, Node b) {
		Double weight = 0.0;
		if (a.hasEdgeFrom(b.getId())
				&& a.<Edge>getEdgeFrom(b.getId()).hasAttribute(weightMarker)) {
			weight = (Double) a.<Edge>getEdgeFrom(b.getId()).getAttribute(
					weightMarker);
		}
		return weight;

	}
	
	/**
	 * Sharc
	 */

	/**
	 * Compute the scores for all relevant communities for the selected node
	 * using the SHARC algorithm
	 * 
	 * @param u
	 *            Node for which the computation is performed
	 * @complexity O(DELTA^2) where DELTA is the average node degree in the
	 *             network
	 */
	protected void communityScores(Node u) {
		/*
		 * Compute the "simple" count of received messages for each community.
		 * This will be used as a fallback metric if the maximum "Sharc" score
		 * is 0, meaning there is no preferred community.
		 */
		epidemicCommunityScores(u);
		communityCounts = communityScores;

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
				// Update score
				if (communityScores.get(v.getAttribute(marker)) == null)
					communityScores.put(v.getAttribute(marker),
							similarity(u, v));
				else
					communityScores.put(v.getAttribute(marker),
							communityScores.get(v.getAttribute(marker))
									+ similarity(u, v));
			}
		}
	}
	
	/**
	 * Compute the scores for all relevant communities for the selected node
	 * using epidemic label propagation paradigm.
	 * 
	 * @param u
	 *            The node for which the scores computation is performed
	 * @complexity O(DELTA) where DELTA is is the average node degree in the
	 *             network
	 */
	protected void epidemicCommunityScores(Node u) {
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
			if (v.hasAttribute(marker))
				if (communityScores.get(v.getAttribute(marker)) == null)
					communityScores.put(v.getAttribute(marker), 1.0);
				else
					communityScores.put(v.getAttribute(marker),
							communityScores.get(v.getAttribute(marker)) + 1.0);
		}
	}

	/**
	 * Neighborhood similarity between two nodes
	 * 
	 * @param a
	 *            The first node
	 * @param b
	 *            The second node
	 * @return The similarity value between the two nodes
	 * @complexity O(DELTA) where DELTA is the average node degree in the
	 *             network
	 */
	protected Double neighborhood_similarity(Node a, Node b) {
		Double similarity = 0.0;
		for (Edge e : a.getEnteringEdgeSet()) {
			Node v = e.getOpposite(a);
			if (!b.hasEdgeFrom(v.getId()))
				similarity += 1.0;
		}
		for (Edge e : b.getEnteringEdgeSet()) {
			Node v = e.getOpposite(b);
			if (!a.hasEdgeFrom(v.getId()))
				similarity += 1.0;
		}
		if (a.getDegree() == 0 && b.getDegree() == 0) 
			return 0.0;
		else
			return 1 - (similarity / (a.getDegree() + b.getDegree()));
	}
	
	/**
	 * My changes
	 * 
	 */
	@Override
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		Node u = graph.getNode(nodeId);
		System.out.println("Remove_originate\t" + graph.getStep() + "\tnode\t" + u.getId() + "\tcom\t" + u.getAttribute(marker) + "\torig\t" + u.hasAttribute(marker + ".originator") + "\tdegree\t" + u.getDegree());
	}
}
