package main.java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.APSP.TargetPath;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.community.Community;
import org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm;
import org.graphstream.algorithm.community.EpidemicCommunityAlgorithm;
import org.graphstream.algorithm.community.Leung;
import org.graphstream.algorithm.community.MobileLeung;
import org.graphstream.algorithm.measure.CommunityDistribution;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolution;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.LayoutRunner;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import com.sun.org.apache.xpath.internal.operations.Bool;

import static org.graphstream.algorithm.Toolkit.*;

public class Crowds {
	
	private FileSource _filesource;
	private Graph _graph;
	private String _ccMarker;
	private String _communityMarker;
	private String _scoreMarker;
	private String[] _printOutMarkers;
	private String[] _mobilityMarkers;
	private Dictionary<String, Object> _algorithmParameters;
	private ConnectedComponents _cc;
	private CommunityDistribution _ccDist;
	private Modularity _modularity;
	private DecentralizedCommunityAlgorithm _algorithm;
	private int _startStep;
	int _stopStep;
	int _numberOfIterations;
	FileSinkImages _fsi;
	Boolean _createImages;
	int _singletons;
	int _connected;
	BufferedWriter _outCommunity;
	String[] _outCommunityHeaders;
	BufferedWriter _outGraph;
	String[] _outGraphHeaders;
	String _sep;
	String _sep2;
//	Dictionary<String, Integer> _communities;
	CommunityDistribution _comDist;
	String _goal = "communities";
	
	public Crowds() {
		_numberOfIterations = 1;
		_startStep = 0;
		_stopStep = 0;
		_createImages = false;
		_singletons = 0;
		_connected = 0;
//		_communities = new Hashtable<String, Integer>();
	}
	
	public void set_createImages(Boolean _createImages) {
		this._createImages = _createImages;
	}

	public int get_numberOfIterations() {
		return _numberOfIterations;
	}

	public void set_numberOfIterations(int _numberOfIterations) {
		this._numberOfIterations = _numberOfIterations;
	}
	
	public void set_printOutMarkers(String[] _printOutMarkers) {
		this._printOutMarkers = _printOutMarkers;
	}
	
	public void set_goal(String goal) {
		this._goal = goal;
	}

	/**
	 * Opens DGS file and initialize graph
	 */
	public void readGraph(String filePath) {
		readGraph(filePath, null, null);
	}

	/**
	 * Opens DGS file and initialize graph
	 */
	public void readGraph(String filePath, String styleSheetUrl, String imagesPrefix) {

		System.out.println("Reading graph from\t" + filePath);
		System.out.println("Goal\t" + _goal);
		try {
			_filesource = FileSourceFactory.sourceFor(filePath);
			_graph = new DefaultGraph("fcd");
			_filesource.addSink(_graph);
		    _filesource.begin(filePath);
			if (_createImages) {
				if (styleSheetUrl == null || styleSheetUrl.isEmpty()) {
					System.err.println("In order to create images, please specify a correct styleSheetUrl."); 
					_createImages = false;
				}
				_graph.addSink(_fsi);
				initializeFsi(styleSheetUrl, imagesPrefix);
			}
		}
		catch (Exception e) {
			System.err.println("Error " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public void initializeConnectedComponents(String marker) {
		initializeConnectedComponents(marker, null);
	}
	
	public void initializeConnectedComponents(String marker, String cutMarker) {
		_ccMarker = marker;
		System.out.println("Marker for connected components and modularity\t\"" + _ccMarker + "\"");
		_cc = new ConnectedComponents(_graph);
		_cc.setCountAttribute(_ccMarker);
		if (cutMarker != null && !cutMarker.isEmpty()) {
			_cc.setCutAttribute("cut");
		}
		_ccDist = new CommunityDistribution(_ccMarker);
		_ccDist.init(_graph);
		_cc.init(_graph);		
		_modularity = new Modularity(_ccMarker);	
		_modularity.init(_graph);
	}
	
	public void setRunningSteps(int startStep, int stopStep) {
		this._startStep = startStep;
		this._stopStep = stopStep;
	}
	
	private Boolean checkPreliminaries() {
		if (_filesource == null) {
			System.err.println("_filesource null. Please call readGraph first.");
			return false;
		}
		if (_startStep == 0 && _stopStep == 0) {
			System.err.println("Please setRunningSteps first.");
			return false;
		}
		return true;
	}
	
	public void detectCommunities(String algorithm, String marker, Dictionary<String, Object> params) {
		if (!checkPreliminaries()) {
			return;
		}
		if (algorithm == null || algorithm.isEmpty() || marker == null || marker.isEmpty()) {
			System.err.println("Algorithm and marker cannot be empty.");
			return;
		}
		// Get algorithm for community detection
		Class algorithmClass;
		try {
			algorithmClass = Class.forName("org.graphstream.algorithm.community." + algorithm);
			_algorithm = (DecentralizedCommunityAlgorithm) algorithmClass.newInstance();
		} catch (Exception e) {
			System.err.println("Error! " + "Algorithm org.graphstream.algorithm.community." + algorithm + " cannot be created.");
			e.printStackTrace();
		} 
		if (_algorithm == null) {
			System.err.println("Error! Algorithm org.graphstream.algorithm.community." + algorithm + " not found.");
			return;
		}
		
		_communityMarker = marker;
		_algorithmParameters = params;
		_algorithm.init(_graph);
		_algorithm.staticMode();
		_algorithm.setMarker(_communityMarker);
		_algorithm.setParameters(_algorithmParameters);
		_communityMarker = _algorithm.getMarker();
		_scoreMarker = _communityMarker + ".score";
		printAlgorithmInfo();
		
		Modularity modularityCom = new Modularity(_communityMarker);	
		if (_goal.equals("communities")) {
			modularityCom.init(_graph);
			_comDist = new CommunityDistribution(_communityMarker);
			_comDist.init(_graph);
		}	
		int step = 0;
		long start_time = System.currentTimeMillis();
		System.out.println("Starting simulation for steps:\t" + _startStep + " - " + _stopStep + " at " + start_time);
		
		try {
			while (_filesource.nextStep()) {
				if (step > _stopStep) {
					break;
				}
				if (step >= _startStep) {
					long currentTime = System.currentTimeMillis();
			    	System.out.println("step:\t" + step + "\tstopStep\t"+_stopStep 
			    			+ "\tnodes\t"+_graph.getNodeCount() + "\tedgess\t"+_graph.getEdgeCount()
			    			+ "\ttime\t" + currentTime + "\tduration\t" + (currentTime - start_time));
	
				}
				if (_goal.equals("communities"))	{
				_cc.compute();
				_ccDist.compute();
					double sumModularity = 0;
					double[] stepsModularity = new double[_numberOfIterations];
					for (int i = 0; i < _numberOfIterations; ++i) {
						_singletons = 0;
						_connected = 0;
						_algorithm.compute();
						modularityCom.compute();
						_comDist.compute();
						double modularityIter = modularityCom.getMeasure();
						stepsModularity[i] = modularityIter;
						sumModularity += modularityIter;
						if (i == (_numberOfIterations - 1)) { // write out nodes information during the the last iteration
							writeNodes(step);
						}
					}
					double avgModularity = sumModularity / _numberOfIterations;
					WriteGraphTimestepStatistics(step, avgModularity, stepsModularity);
				}
				++step;
			}
			printSummary(System.currentTimeMillis()-start_time);
			if (_goal.equals("ASPL")) {
				System.out.println("Computing aspl for step:\t" + step);
				computeASPL();
			}
			_filesource.end();
			if (_createImages) {
				_fsi.end();
			}
			_outGraph.close();
			_outCommunity.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			_filesource.removeSink(_graph);
		}
	    return;
	}
	
	private void computeASPL() {
		APSP apsp = new APSP();
        apsp.init(_graph); // registering apsp as a sink for the graph
        apsp.setDirected(false); // undirected graph
        apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight"
        apsp.compute(); // the method that actually computes shortest paths
        double allSum = 0;
        for (Node node1 : _graph.getNodeSet()) {
			String id1 = node1.getId(); 
        	APSPInfo info = _graph.getNode(id1).getAttribute(APSPInfo.ATTRIBUTE_NAME);
//        	System.out.println(info.getShortestPathTo(id2) + ", " + info.getShortestPathTo(id2).getEdgeCount() + ", length: " + info.getMinimumLength());
			double sumShortest = 0;	
			HashMap<String, TargetPath>	targets = info.targets;
			if (targets.size() > 0) {
				for (TargetPath path : targets.values()) {
					sumShortest += path.distance;
	//				System.out.println(id1 + " shortest path to the target " +path.target.getId()  + "= " + path.distance);
				}
				double avgShortest = sumShortest / targets.size();
				allSum += avgShortest;
				//System.out.println(id1 + " avg shortest path to all targets " + targets.size() + "= " + avgShortest);
			}
        }
//        System.out.println(" allSum " + allSum);
//        System.out.println(" _graph.getNodeCount() " + _graph.getNodeCount());
        double allAvgShortest = allSum / (double)_graph.getNodeCount();
        System.out.println(" avg shortest path to all targets " + allAvgShortest);
	}
	
	private void printAlgorithmInfo() {
		System.out.println("Running community detection ... ");
		System.out.println("algorithm\t" + _algorithm);
		System.out.println("parameters\t" + _algorithmParameters);
		System.out.println("community marker\t" + _communityMarker);
	}
	
	private void printSummary(long duration) {
		System.out.println("Finish simulation at " + System.currentTimeMillis() + ", simulation time: " + duration);
		System.out.println("number of steps\t" + _graph.getStep());
		System.out.println("Number of nodes\t" + _graph.getNodeCount());
		System.out.println("Number of edges\t" + _graph.getEdgeCount());
	}
	
	public void openOutputFiles(String communityOutputFile, String communityFileDataFormat, String graphOutputFile, String graphFileDataFormat, String sep, String sep2) {
		System.out.println("Open output files\t" + communityOutputFile + ", " + graphOutputFile);
		_sep = sep;
		_sep2 = sep2;
		try {
			_outCommunity = new BufferedWriter(new FileWriter(communityOutputFile));
			_outCommunityHeaders = communityFileDataFormat.split(_sep);
			_outCommunity.write(communityFileDataFormat);
			_outCommunity.newLine();
			_outGraph = new BufferedWriter(new FileWriter(graphOutputFile));
			_outGraphHeaders = graphFileDataFormat.split(_sep);
			_outGraph.write(graphFileDataFormat);
			_outGraph.newLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * "step"
	 * "node_id" "x" "y"
	 * "degree" "neighbors"
	 * "cc_id" "cc_size" 
	 * "com_id" "com_score" "com_size";
	 * @param step
	 */
    private void writeNodes(int step) {
		for (Node node : _graph.getNodeSet()) {
			Dictionary<String, Object> values = new Hashtable<String, Object>();
			values.put("step", step);
			values.put("node_id", node.getId());
			Object cc = node.getAttribute(_ccMarker);
			values.put("cc_id", cc);
			values.put("cc_size", _ccDist.communitySize(cc));
			for (String marker : _printOutMarkers) {
				values.put(marker, node.getAttribute(marker));
			}
			Community community = (Community)node.getAttribute(_communityMarker);
			String communityId = community.getId();
			values.put("com_id", communityId);
			values.put("com_size", _comDist.communitySize(community));
			if (_scoreMarker != null && !_scoreMarker.isEmpty()) {
				values.put("com_score", node.getAttribute(_scoreMarker));
			}
			int degree = node.getEdgeSet().size();
			values.put("degree", degree);
			String neighbors = "";
//			for (Edge edge : node.getEdgeSet()) {
//				neighbors+=edge.getOpposite(node).getId()+_sep2;
//			}
			values.put("neighbors", neighbors);
			if (degree > 0) {
				++_connected;
			}
			else {
				++_singletons;
			}
			try {
				int headersCount = _outCommunityHeaders.length;
				int i = 0;
				for (String column : _outCommunityHeaders) {
					if (++i < headersCount) {	
						_outCommunity.write(values.get(column)+_sep);
					}
					else {
						_outCommunity.write(values.get(column) + "\n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
    /** Writes cvs file with following columns:
     * "step" "nodes" "edges" "singletons" "connected"
     * "avg_degree" "degree_distribution" "diameter" "avg_clustering_coefficient"
     * "cc_count" "avg_cc_size" "max_cc_size" "max_cc_id" "cc_std_dev" 
     * "com_count" "avg_com_size" "max_com_size" "max_com_id" "min_com_size" "min_com_id" "std_com_dist"
     * "modularity" "com_modularity" "iterations" "iteration_modularities";
     * @param step Current step
     * @param avgModularity average modularity of detected community structure
     * @param stepsModularity modularity for each iteration step
     * @param _comDist community statistics 
     * @throws IOException
     */
	public void WriteGraphTimestepStatistics(int step, double avgModularity, double[] stepsModularity) throws IOException {
		Dictionary<String, Object> values = new Hashtable<String, Object>();
		values.put("step", step);
		values.put("nodes", _graph.getNodeCount());
		values.put("edges", _graph.getEdgeCount());		
		values.put("singletons", _singletons);
		values.put("connected", _connected);
		
		// stats
		Toolkit toolkit = new Toolkit();
		values.put("avg_degree", toolkit.averageDegree(_graph));
		int[] degreeDistribution = toolkit.degreeDistribution(_graph);
		if (degreeDistribution != null) {
			String degreeDistributionStr = "";
			for (int j = 0; j < degreeDistribution.length; ++j) {
				degreeDistributionStr += degreeDistribution[j] + _sep2;
			}
			values.put("degree_distribution",degreeDistributionStr);
		}
		values.put("diameter", toolkit.diameter(_graph));
		Double coef = toolkit.averageClusteringCoefficient(_graph);
		values.put("avg_clustering_coefficient", toolkit.averageClusteringCoefficient(_graph));

		// cc "cc_count" "avg_cc_size" "max_cc_size" "max_cc_id" "cc_std_dev"
		values.put("cc_count",_cc.getConnectedComponentsCount());
		values.put("avg_cc_size", _ccDist.average());
		List<Node> giantComponent = _cc.getGiantComponent();
		int ccId = -1;
		if (giantComponent.size() > 0) {
			Node node = giantComponent.get(0);
			ccId = (Integer)node.getAttribute(_ccMarker);
		}
		values.put("max_cc_size",giantComponent.size());
		values.put("max_cc_id", ccId);
		values.put("cc_std_dev", _ccDist.average());
		
		// communities "com_count" "avg_com_size" "max_com_size" "max_com_id" "min_com_size" "min_com_id" "std_com_dist"
		values.put("com_count",_comDist.number());
		values.put("avg_com_size", _comDist.average());
		values.put("max_com_size", _comDist.maxCommunitySize());
		values.put("max_com_id", (Community)_comDist.biggestCommunity());
		values.put("min_com_size", _comDist.minCommunitySize());
		values.put("min_com_id",(Community)_comDist.smallestCommunity());
		values.put("std_com_dist",_comDist.stdev());
		
		// modularity
		values.put("modularity",_modularity.getMeasure());
		values.put("com_modularity",avgModularity);
		values.put("iterations",_numberOfIterations);
		if (stepsModularity != null) {
			String modularities = "";
			for (int i = 0; i < stepsModularity.length; ++i) {
				modularities += stepsModularity[i]+ _sep2;
			}
			values.put("iteration_modularities", modularities);
		}
		
		int headersCount = _outGraphHeaders.length;
		int i = 0;
		for (String column : _outGraphHeaders) {
			if (++i != headersCount) {
				_outGraph.write(values.get(column) + _sep);
			}
			else {
				_outGraph.write((String)values.get(column));
			}
		}
		
		_outGraph.newLine();
	}

	
	public void initializeFsi(String styleSheetUrl, String prefix) throws IOException {
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
		FileSinkImages _fsi = new FileSinkImages(prefix, type, resolution, outputPolicy );
		String styleSheet = styleSheetUrl;
		_fsi.setStyleSheet(styleSheet);
		_fsi.setLayoutPolicy( LayoutPolicy.NO_LAYOUT);
		_fsi.begin(prefix);
	}
	
	
	

//	public void WriteNode(BufferedWriter out, String separator, String separatorEdges, int step, Node node, HashMap<Marker, String> markers) throws IOException {
//		String nodeId = node.getId();
//		Integer module = (Integer)node.getAttribute(markers.get(Marker.MODULE));
//		Double x = (Double)node.getAttribute(markers.get(Marker.X));
//		Double y = (Double)node.getAttribute(markers.get(Marker.Y));
//		Community community = (Community)node.getAttribute(markers.get(Marker.COMMUNITY));
//		Double communityScore = (Double)node.getAttribute(markers.get(Marker.COMMUNITY_SCORE));
//		int degree = node.getEdgeSet().size();
//		out.write(step+separator+nodeId+separator+x+separator+y+separator+degree+separator);
//		for (Edge edge : node.getEdgeSet()) {
//			out.write(edge.getOpposite(node).getId()+separatorEdges);
//		}
//		out.write(separator+community.id()+separator+communityScore+separator+module);
//		out.newLine();
//	}
	
//	public void computeAttributesForCommunityDetection() {
//		String xMarker = "x";
//		String yMarker = "y";
//		Double maxDistance = 0.0;
//		Double sumDistance = 0.0;
//		int edgeCount = 0;
//		Collection<Node> neighbors = _graph.getNodeSet();
//		for (Node node : neighbors) {
//			String nodeId = node.getId();
//			Double nodeX = (Double)node.getAttribute(xMarker);
//			Double nodeY = (Double)node.getAttribute(yMarker);
//			for (Edge edge : node.getEdgeSet()) {
//				Node neighbor = edge.getOpposite(node);
//				Double neighborX = (Double)neighbor.getAttribute(xMarker);
//				Double neighborY = (Double)neighbor.getAttribute(yMarker);
//				Double distance = calculateDistance(nodeX, nodeY, neighborX, neighborY);
//				// calculate distance
//				edge.setAttribute(_communityMarker, distance);
//				sumDistance += distance;
//				edgeCount ++;
//				if (distance > maxDistance) {
//					maxDistance = distance;
//				}
//			}
//		}
////		if (edgeCount > 0)
////		System.out.println("max distance: " + maxDistance + " average distance: " + sumDistance / edgeCount);
//	}
//	
//	public Double calculateDistance(Double nodeX, Double nodeY, Double neighborX, Double neighborY) {
//		Double distance = Math.sqrt((nodeX-neighborX)*(nodeX-neighborX) + (nodeY-neighborY)*(nodeY-neighborY));
//		return distance;
//	}
	
//	public void createMovie(String filePath, String styleSheetUrl, String prefix) {
//		// FileSinkImages
//		OutputType type = OutputType.PNG;
//		Resolution resolution = Resolutions.HD720;
//		OutputPolicy outputPolicy = OutputPolicy.BY_STEP;
//		FileSinkImages _fsi = new FileSinkImages(prefix, type, resolution, outputPolicy );
//		String styleSheet = styleSheetUrl;
//		_fsi.setStyleSheet(styleSheet);
//		_fsi.setLayoutPolicy( LayoutPolicy.NO_LAYOUT);
////		_fsi.addLogo( "path/to/logo", x, y );
//		try {
//			FileSourceDGS dgs = new FileSourceDGS();
//			dgs.begin(filePath);
//			dgs.addSink(_fsi);
//			_fsi.begin(prefix);
//			while ( dgs.nextStep() ) {
//				System.out.println("next step..");
//			}
//			dgs.end();
//			_fsi.end();
//		}
//		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public void display(Graph graph, String styleSheetUrl) {
//		graph.addAttribute("ui.stylehseet", styleSheetUrl);
//		graph.addAttribute("ui.quality");
//		graph.addAttribute("ui.antialias");
//		SpriteManager spriteManager = new SpriteManager(graph);
//		Sprite spriteTime = spriteManager.addSprite("SpriteTime");
//		spriteTime.setPosition(Units.PX, 12, 180, 0);
//		
//		Viewer viewer = graph.display();
//		View view = viewer.getDefaultView();
//		view.resizeFrame(800, 600);
//		
//		int i = 0;
//		while(!graph.hasAttribute("ui.viewClosed")) {
//			spriteTime.setAttribute("ui.label", String.format("Time %d", i++));
//			try {
//				viewer.wait(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
}
