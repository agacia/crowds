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

//import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Random;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.community.MobilityMeasure;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.Sink;

/**
 * 
 * author Agata Grzybek
 * 
 */
public class CongestionMeasure implements
Algorithm, Sink {
	/**
	* The graph to apply the algorithm.
	*/
	protected Graph graph;

//	private static final Integer STOPS_NUMBER = 2;
	private static final Integer CYCLE_TIME = 90;
	private static final Double STOP_SPEED = 1.0;
	
	/**
	 * Name of the marker that is used to store weight of links on the graph
	 * that this algorithm is applied to.
	 */
	protected String weightMarker = "weight";
	protected String speedMarker = "vehicleSpeed";
	protected String dynamismMarker = "dynamism";
	protected String timeMeanSpeedMarker = "timeMeanSpeed";
	protected String speedHistoryMarker = "speedHistory";
	protected String speedHistoryTimestampsMarker = "speedHistoryTimestamps";
	protected String speedHistoryIndexMarker = "speedHistoryIndex";
	protected String maxHistoryRecordsMarker = "maxHistoryRecords";
	protected String angleMarker = "vehicleAngle";
	protected String laneMarker = "vehicleLane";
	protected String linkDurationMarker = "linkDuration";
	protected String mobilitySimilarityMarker = "mobilitySimilarity";
	protected String hybridMarker = "hybrid";
	protected String speedType = "timemean"; // or 'instant' , 'spacetimemean'
	protected Integer speedHistoryLength = 0;
	

	public CongestionMeasure() {
		super();
	}

	public CongestionMeasure(Graph graph) {
		this();
		init(graph);
	}

	/**
	 * Create a new algorithm instance, attached to the specified graph,
	 * using the specified weightMarker to retrieve the weight attribute of graph edges.
	 * 
	 * @param graph
	 *            graph to which the algorithm will be applied
	 * @param weightMarker
	 *            edge weight marker
	 */
	public CongestionMeasure(Graph graph, String weightMarker) {
		this();
		init(graph);
		this.weightMarker = weightMarker;
	}
	
	/**
	 * Sets the preference exponent and hop attenuation factor to the given
	 * values.
	 * 
	 */
	public void setParameters(Dictionary<String, Object> params) {
		this.speedMarker = (String) params.get("speedMarker");
		this.laneMarker = (String) params.get("laneMarker");
		this.weightMarker = (String) params.get("weightMarker");
		this.dynamismMarker = (String) params.get("dynamismMarker");
		if (params.get("angleMarker") != null) {
			this.angleMarker = (String) params.get("angleMarker");
		}
		if (params.get("speedHistoryLength") != null) {
			this.speedHistoryLength = (Integer) params.get("speedHistoryLength");
		}
		if (params.get("speedType") != null) {
			this.speedType = (String) params.get("speedType");
		}
		if (params.get("timeMeanSpeedMarker") != null) {
			this.timeMeanSpeedMarker = (String) params.get("timeMeanSpeedMarker");
		}
		if (params.get("linkDurationMarker") != null) {
			this.linkDurationMarker = (String) params.get("linkDurationMarker");
		}
		if (params.get("mobilitySimilarityMarker") != null) {
			this.mobilitySimilarityMarker = (String) params.get("mobilitySimilarityMarker");
		}
		
		
	}
	
	/**
	 * Random number generator used to shuffle the nodes. Shall be used by all
	 * inherited algorithms for random number generation
	 */
	protected Random rng;
	
	/**
	 * Compute an iteration of the algorithm for all the nodes of the network.
	 * 
	 * @complexity N times the complexity of the computeNode() function, where N
	 *             is the number of nodes in the network.
	 */
//	@Override
	public void compute() {
		/*
		 * This simply calls the computeNode method for all nodes in the graph.
		 * Nodes are processed in a random order. Computation only occurs if the
		 * graph has changed since last call
		 */
		ArrayList<Node> nodeSet = new ArrayList<Node>(graph.getNodeSet());
		Collections.shuffle(nodeSet, rng);
		for (Node node : nodeSet) {
			computeNode(node);
		}
		ArrayList<Edge> edgeSet = new ArrayList<Edge>(graph.getEdgeSet());
		for (Edge edge : edgeSet) {
			computeEdge(edge);
			
			
		}
	}
	
	protected Double n_similarity(Node a, Node b) {
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
		else if (a.getDegree() == 1 || b.getDegree() == 1)
			return 1.0;
		else
			return 1 - (similarity / (a.getDegree() + b.getDegree()));
	}
	
	/**
	 * Count up edge interaction
	 * @param edge
	 */
	private void computeEdge(Edge edge) {
		if (!edge.hasAttribute(this.linkDurationMarker)) {
			edge.setAttribute(this.linkDurationMarker, 0.0);	
		}
		Double linkDuration = (Double)edge.getAttribute(this.linkDurationMarker);
		linkDuration++;
		edge.setAttribute(this.linkDurationMarker, linkDuration);
		String speedM = speedMarker;
		if (speedType.equals("timemean")) {
			speedM = this.timeMeanSpeedMarker;
		}
		
		Double mobSim = MobilityMeasure.computeRelativeMobility(edge.getNode0(), edge.getNode1(), speedM, angleMarker);	
		edge.setAttribute(this.mobilitySimilarityMarker, mobSim);
		Double hybrid = linkDuration * mobSim;
		edge.setAttribute(this.hybridMarker, hybrid);
		Node a = edge.getNode0();
		Node b = edge.getNode1();
		Double n_sim = n_similarity(a, b);
		edge.setAttribute("n_sim", n_sim);
//		if (edge.getId().equals("from1.4-from1.5")) {
//			Double speedU = MobilityMeasure.getMarkerValue(a, speedMarker);
//			Double speedV = MobilityMeasure.getMarkerValue(b, speedMarker);
//			Double angleU = MobilityMeasure.getMarkerValue(a, angleMarker);
//			Double angleV = MobilityMeasure.getMarkerValue(b, angleMarker);
//			System.out.println(graph.getStep() + " Edge\t" + edge.getId() + ", duration: " + linkDuration + " " + this.linkDurationMarker + ", nsim: " + n_sim + ", mob sim: " + mobSim + " " + this.mobilitySimilarityMarker + " hybrid " + hybrid + " speedU " + speedU + " speedV " + speedV + " angleU " + angleU + " angleV " + angleV);
//		}
	}
	
	public void computeNode(Node node) {
		setNumberOfStopsOnALane(node);
		setAverageSpeed(node);
		setDynamism(node, this.timeMeanSpeedMarker, this.dynamismMarker);
	}
	
	public Double getInstantaneousSpeed(Node node) {
		Double speed = 0.0;
		if (node.hasAttribute(speedMarker)) {
			speed = (Double)node.getAttribute(speedMarker);
		}
		return speed;
	}
	
	public String getSpeedType() {
		return speedType;
	}
	
	protected void setAverageSpeed(Node node) {
		if (speedType.equals("timemean") || speedType.equals("instant")) {
			Double speed = getInstantaneousSpeed(node);
			addSpeedToHistory(node, speed);
			//System.out.println("build history of instant speeds");
		}
		else if (speedType.equals("spacetimemean")) {
			Double spaceMeanSpeed = calculateSpaceMeanSpeed(node);
			addSpeedToLinkSpeedHistory(node, spaceMeanSpeed);
//			System.out.println("build history of spaceMeanSpeed");
		}
		calculateAndSetTimeMeanSpeed(node);
	}
	
	class MutableInteger {
	    public int value;
	}
	
	/***
	 * Adds current instant speed of the vehicle to its speed history.
	 * Initializes the history with zeros if the vehicle has started traveling
	 * Sets 
	 * - speedHistoryMarker with the array of historical records
	 * - speedHistoryTimestampsMarker with the array of historical timestamps
	 * - speedHistoryIndexMarker - current index in historical arrays
	 * - maxHistoryRecordsMarker -1 because the vehicle always uses its own history (never copies from a neighbor)
	 * @param node
	 * @param speed
	 */
	public void addSpeedToHistory(Node node, Double speed) {
		Double[] history = new Double[speedHistoryLength];
		Double[] timestamps = new Double[speedHistoryLength];
		Integer speedHistoryIndex = 0;
		// initialise if just started traveling
		if (!node.hasAttribute(speedHistoryMarker)) {
			for (int i = 0; i < speedHistoryLength; ++i) {
				history[i] = 0.0;
				timestamps[i] = 0.0;
			}
		}
		else {
			history = (Double[])node.getAttribute(speedHistoryMarker);
			timestamps = (Double[])node.getAttribute(speedHistoryTimestampsMarker);
			speedHistoryIndex = (Integer)node.getAttribute(speedHistoryIndexMarker);
		}
		Double now = graph.getStep();
		// add the value to the history 
		history[speedHistoryIndex%speedHistoryLength] = speed;
		timestamps[speedHistoryIndex%speedHistoryLength] = now;
//		System.out.println("Writing to history node " + node.getId() + " time/speed: " + now + " " + speed );
		speedHistoryIndex++;
		// save attributes on node
		node.setAttribute(speedHistoryMarker, (Object[])history);
		node.setAttribute(speedHistoryTimestampsMarker, (Object[])timestamps);
		node.setAttribute(speedHistoryIndexMarker, speedHistoryIndex);	
		node.setAttribute(maxHistoryRecordsMarker, -1);
	}
	
	/***
	 * Adds the space-mean speed of the vehicle to its speed history. The speed history is relevant only on a link
	 * Copies a neighbor's history whenever a vehicle changes link or starts traveling  (from a neighbor with the longest history) is possible
	 * Initializes the history with zeros if no neighbors on the link
	 * Sets 
	 * - speedHistoryMarker with the array of historical records
	 * - speedHistoryTimestampsMarker with the array of historical timestamps
	 * - speedHistoryIndexMarker - current index in historical arrays
	 * - maxHistoryRecordsMarker - 0 if vehicle restarts history and fills it with all 0, > 0 if a vehicle copies historical speeds from a neighbor, -1 if vehicle used its own historical speeds
	 * @param node
	 * @param speed
	 */
	public void addSpeedToLinkSpeedHistory(Node node, Double spaceMeanSpeed) {
		Double[] history = new Double[speedHistoryLength];
		Double[] timestamps = new Double[speedHistoryLength];
		Double now = graph.getStep();
		
		for (int i = 0; i < speedHistoryLength; ++i) {
			history[i] = 0.0;
			timestamps[i] = 0.0;
		}
		Integer speedHistoryIndex = 0;
		String currentLane = (String) node.getAttribute(this.laneMarker).toString();
		String previousLane = (String) node.getAttribute(this.laneMarker + ".previous").toString();

//		DecimalFormat df = new DecimalFormat("##.##");
		MutableInteger maxHistoryRecords = new MutableInteger();
		maxHistoryRecords.value = -1;
		
		// get speed history from a neighbor
		// if node just started traveling  or if node changed a link
		if (!node.hasAttribute(speedHistoryMarker) || !previousLane.equals(currentLane)) {
			Node maxHistoryNode = getMaxHistoryNode(node, currentLane, maxHistoryRecords);
			// copy the history records from the max node
			if (maxHistoryNode != null) {
				Double[] maxHistory = (Double[])maxHistoryNode.getAttribute(speedHistoryMarker);
				int i = 0;
				Double[] maxHistoryTimestamps = (Double[])maxHistoryNode.getAttribute(speedHistoryTimestampsMarker);
				int speedHistoryIndexNeighbor = (Integer)maxHistoryNode.getAttribute(speedHistoryIndexMarker);
				for (int j = (speedHistoryIndexNeighbor); j<speedHistoryLength+speedHistoryIndexNeighbor; j++) {
					if (maxHistoryTimestamps[j%speedHistoryLength]!=0.0 && (now-maxHistoryTimestamps[j%speedHistoryLength]) < speedHistoryLength && !now.equals(maxHistoryTimestamps[j%speedHistoryLength]))  {
						history[i] = maxHistory[j%speedHistoryLength];
						timestamps[i] = maxHistoryTimestamps[j%speedHistoryLength];
						i++;
					}
				}
				speedHistoryIndex = i;
			} else {
				maxHistoryRecords.value = 0;
			}
		}
		else if (node.hasAttribute(speedHistoryMarker) && previousLane.equals(currentLane)) {
			// get vehicle's own history
			history = (Double[])node.getAttribute(speedHistoryMarker);
			timestamps = (Double[])node.getAttribute(speedHistoryTimestampsMarker);
			speedHistoryIndex = (Integer)node.getAttribute(speedHistoryIndexMarker);
			maxHistoryRecords.value = -1;
		}
		
		// add the value to the history 
		//System.out.println(node.getId() + " writing to history its own speed at index " + speedHistoryIndex%speedHistoryLength + ", spaceMeanSpeed: " + spaceMeanSpeed + ", timestamp: " +  graph.getStep() );
		history[speedHistoryIndex%speedHistoryLength] = spaceMeanSpeed;
		timestamps[speedHistoryIndex%speedHistoryLength] = graph.getStep();
		speedHistoryIndex++;
		// save on node
		node.setAttribute(speedHistoryMarker, (Object[])history);
		node.setAttribute(speedHistoryTimestampsMarker, (Object[])timestamps);
		node.setAttribute(speedHistoryIndexMarker, speedHistoryIndex);	
		node.setAttribute(maxHistoryRecordsMarker, maxHistoryRecords.value);
	}
	
	public Node getMaxHistoryNode(Node node, String currentLane, MutableInteger maxHistoryRecords) {
		Node maxHistoryNode = null;
		Double now = graph.getStep();
		for (Edge e : node.getEnteringEdgeSet()) {
			int historyRecords = 0;
			Node neighbor = e.getOpposite(node);
			if (neighbor.hasAttribute(laneMarker) && ((String)(neighbor.getAttribute(laneMarker)).toString()).equals(currentLane)) {
				// if neighbour was already on the lane 
				String neighborLane = (String)(neighbor.getAttribute(laneMarker)).toString();
				if (neighbor.hasAttribute(laneMarker+".previous") && ((String)(neighbor.getAttribute(laneMarker+".previous")).toString()).equals(neighborLane)) {
					if (neighbor.hasAttribute(speedHistoryMarker)) {				
						Double[] neighborHistory = (Double[])neighbor.getAttribute(speedHistoryMarker);
						Double[] neighborTimestamps = (Double[])neighbor.getAttribute(speedHistoryTimestampsMarker);
						// get a neighbour history with the most number of nonzero history records
						if (neighborHistory.length > 0) {
							for (int i = 0; i < neighborHistory.length; ++i) {
								if (neighborTimestamps[i]!=0.0 && (now-neighborTimestamps[i]) < speedHistoryLength && !now.equals(neighborTimestamps[i]))  {
									historyRecords ++;
//									System.out.println(now + " counting to  history for i" + i + " " + neighborHistory[i] + ", timestamp: " + neighborTimestamps[i]);
									
								}
							}
							if (historyRecords > maxHistoryRecords.value) {
								maxHistoryNode = neighbor;
								maxHistoryRecords.value = historyRecords;
							}
						}
					}
				}
			}
		}
		return maxHistoryNode;
	}
	
	/***
	 * Calculates the average speed of all neighbors traveling on the same links as the node
	 * @param node
	 * @return the average speed of all neighbors traveling on the same links as the node
	 */
	public Double calculateSpaceMeanSpeed(Node node) {
		Double spaceMeanSpeed = 0.0;
		String myLane = "";
		if (node.hasAttribute(laneMarker) && node.hasAttribute(speedMarker)) {
			ArrayList<Double> speeds = new ArrayList<Double>();
			myLane = (String) (node.getAttribute(laneMarker).toString());
			Double mySpeed = (Double)node.getAttribute(speedMarker);
			//Iterate over the nodes that this node "hears"
			for (Edge e : node.getEnteringEdgeSet()) {
				Node neighbor = e.getOpposite(node);
				// check if neighbor is on the same lane
				if (neighbor.hasAttribute(laneMarker)) {
					String neighborLane = (String) (neighbor.getAttribute(laneMarker).toString());
					if (neighborLane.equals(myLane)) {
						// add neighbor's speed to speeds 
						Double neighborSpeed = (Double)neighbor.getAttribute(speedMarker);
						speeds.add(neighborSpeed);
					}
				}
			}
			// calculte mean
			Double sumSpeed = mySpeed;
			for (int i = 0; i < speeds.size(); ++i) {
				sumSpeed += speeds.get(i);
			}
			spaceMeanSpeed = sumSpeed / (1+speeds.size());
		}
		return spaceMeanSpeed;
	}
	
	public Double calculateAndSetTimeMeanSpeed(Node node) {
		Double sumSpeed = 0.0;
		int sumNonZeroCount = 0;
		Double timeMeanSpeed = 0.0;
		Double[] speeds = (Double[])node.getAttribute(speedHistoryMarker);
		Double[] timestamps = (Double[])node.getAttribute(speedHistoryTimestampsMarker);
		Double now = graph.getStep();
//		DecimalFormat df = new DecimalFormat("##.##");
//		System.out.println("timestamps node " + node.getId() + " ");
		if (timestamps != null) {
			for (int i = 0; i < timestamps.length; ++i) {
	//			System.out.print("\t"+timestamps[i]);
				// use for calculation of the time average the seconds from the whole cycle 
				if (timestamps[i]!=0.0 && (now-timestamps[i]) <= speedHistoryLength) {
					sumSpeed += speeds[i];
					sumNonZeroCount ++;
				}
			}
		}
		if (sumNonZeroCount > 0) {
			timeMeanSpeed = sumSpeed / sumNonZeroCount;
		}
//		System.out.println("node " + node.getId() +" timeMeanSpeed " + timeMeanSpeed + " historical records: " + timestamps.length + ", nonzero: " + sumNonZeroCount);
		node.setAttribute(timeMeanSpeedMarker, timeMeanSpeed);
		node.setAttribute(timeMeanSpeedMarker+".count", sumNonZeroCount);
		return timeMeanSpeed;
	}
	
	protected void setDynamism(Node node, String propertyMarker, String dynamismMarker) {
		if (node.hasAttribute(propertyMarker)) {
			Double speed = (Double) node.getAttribute(propertyMarker);
			String previousMarker = propertyMarker + ".previous";
			// first time
			if (!node.hasAttribute(previousMarker)) {
				node.addAttribute(previousMarker, speed);
				node.addAttribute(dynamismMarker, 0.0);
			}
			// next time
			else {
				Double previousSpeed = node.getAttribute(previousMarker);
				Double dynamism = speed - previousSpeed;
				node.setAttribute(previousMarker, speed);
				node.setAttribute(dynamismMarker, dynamism);
			}
		}
	}
	
	protected void setNumberOfStopsOnALane(Node node) {
		if (node.hasAttribute(this.laneMarker)) {
			String lane = (String)node.getAttribute(this.laneMarker).toString();
			if (!node.hasAttribute(this.laneMarker + ".stops")) {
				node.addAttribute(this.laneMarker + ".previous", lane);
				node.addAttribute(this.laneMarker + ".current", lane);
				node.addAttribute(this.laneMarker + ".stops", 0);
				node.addAttribute(this.laneMarker + ".probes", 0);
				node.addAttribute(this.laneMarker + ".stopTime", 0.0);
			}
			String currentLane = (String) node.getAttribute(this.laneMarker + ".current").toString();
			if (!lane.equals(currentLane)) {
				// new lane 
				node.setAttribute(this.laneMarker + ".previous", currentLane);
				node.setAttribute(this.laneMarker + ".current", lane);
				node.setAttribute(this.laneMarker + ".stops", 0);
				node.setAttribute(this.laneMarker + ".probes", 0);
				node.setAttribute(this.laneMarker + ".stopTime", 0.0);
			}
			Integer probesAtCurrentLane = (Integer)node.getAttribute(this.laneMarker + ".probes");
			node.setAttribute(this.laneMarker + ".probes", ++probesAtCurrentLane);
		
			if (node.hasAttribute(this.speedMarker)) {
				Double speed = (Double) node.getAttribute(this.speedMarker);
				
				Double stopTime = (Double)node.getAttribute(this.laneMarker + ".stopTime");
				Integer stopsAtCurrentLane = (Integer)node.getAttribute(this.laneMarker + ".stops");
				if (speed != null && speed <= STOP_SPEED) {
					Double currentStep = graph.getStep();
					// stopped for the first time on the link
					// or if stopped again after the whole cycle time passed count as a next stop
					if (stopTime==0.0 || (currentStep-stopTime) > CYCLE_TIME) {
						node.setAttribute(this.laneMarker + ".stopTime", currentStep);
						node.setAttribute(this.laneMarker + ".stops", ++stopsAtCurrentLane);
					}
				}
			}
		}
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub
		
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub
		
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		// TODO Auto-generated method stub
		
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		// TODO Auto-generated method stub
		
	}

	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub
		
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		// TODO Auto-generated method stub
		
	}

	public void init(Graph graph) {
		this.graph = graph;
		if (this.rng == null)
			rng = new Random();
	}

	
}